package com.raccoon.healthmonitoring.auth.service;

import com.raccoon.healthmonitoring.auth.dto.LoginRequestDto;
import com.raccoon.healthmonitoring.auth.dto.RegisterRequestDto;
import com.raccoon.healthmonitoring.auth.dto.TokenResponse;
import com.raccoon.healthmonitoring.auth.enums.TokenType;
import com.raccoon.healthmonitoring.auth.exception.AccountLockedException;
import com.raccoon.healthmonitoring.auth.model.Token;
import com.raccoon.healthmonitoring.auth.model.UserPrincipal;
import com.raccoon.healthmonitoring.auth.repository.TokenRepository;
import com.raccoon.healthmonitoring.users.User;
import com.raccoon.healthmonitoring.users.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public TokenResponse register(RegisterRequestDto request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User savedUser = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(savedUser);

        UserPrincipal userPrincipal = new UserPrincipal(savedUser);
        String jwtToken = jwtService.generateToken(new HashMap<>(), userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), userPrincipal);
        saveUserToken(savedUser, jwtToken);
        return new TokenResponse(jwtToken, refreshToken);
    }

    public TokenResponse login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User does not exist with the provided email"));

        if (loginAttemptService.isBlocked(request.email())) {
            LocalDateTime blockedUntil = loginAttemptService.getLockedUntil(request.email());
            throw new AccountLockedException(
                    "Account is locked due to multiple failed login attempts. Try again at " + blockedUntil,
                    blockedUntil
            );
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            loginAttemptService.loginSucceeded(request.email());

            UserPrincipal userPrincipal = new UserPrincipal(user);
            String jwtToken = jwtService.generateToken(new HashMap<>(), userPrincipal);
            String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), userPrincipal);
            revokeAllUserTokens(user);
            saveUserToken(user, refreshToken);
            return new TokenResponse(jwtToken, refreshToken);
        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(request.email());
            int remainingAttempts = loginAttemptService.getRemainingAttempts(request.email());

            if (remainingAttempts > 0) {
                throw new BadCredentialsException(
                        "Invalid credentials. You have " + remainingAttempts + " more attempt(s) before your account gets locked."
                );
            } else {
                LocalDateTime blockedUntil = loginAttemptService.getLockedUntil(request.email());
                throw new AccountLockedException(
                        "Account is locked due to multiple failed login attempts. Try again at " + blockedUntil,
                        blockedUntil
                );
            }
        }
    }

    public TokenResponse refreshToken(String authentication) {
        String refreshToken = authentication.substring(7);
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            return null;
        }

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        UserPrincipal userPrincipal = new UserPrincipal(user);
        boolean isTokenValid = jwtService.isRefreshTokenValid(refreshToken, userPrincipal);
        if (!isTokenValid) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String accessToken = jwtService.generateToken(new HashMap<>(), userPrincipal);
        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(String authHeader) {
        String refreshToken = authHeader.substring(7);

        tokenRepository.findByToken(refreshToken).ifPresent(storedToken -> {
            storedToken.setIsExpired(true);
            storedToken.setIsRevoked(true);
            tokenRepository.save(storedToken);
        });
    }

    private void saveUserToken(User user, String jwtToken) {
        Token tokenEntity = Token.builder()
                .token(jwtToken)
                .user(user)
                .tokenType(TokenType.BEARER)
                .isRevoked(false)
                .isExpired(false)
                .build();
        tokenRepository.save(tokenEntity);
    }

    private void revokeAllUserTokens(final User user) {
        final List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.setIsExpired(true);
                token.setIsRevoked(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }
}
