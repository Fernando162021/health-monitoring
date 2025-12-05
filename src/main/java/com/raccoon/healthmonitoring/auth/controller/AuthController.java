package com.raccoon.healthmonitoring.auth.controller;

import com.raccoon.healthmonitoring.auth.dto.LoginRequestDto;
import com.raccoon.healthmonitoring.auth.dto.RegisterRequestDto;
import com.raccoon.healthmonitoring.auth.dto.TokenResponse;
import com.raccoon.healthmonitoring.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("Registration attempt for email: {}", request.email());
        TokenResponse tokenResponse = authService.register(request);
        log.info("User registered successfully: {}", request.email());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Login attempt for email: {}", request.email());
        TokenResponse tokenResponse = authService.login(request);
        log.info("Login successful for email: {}", request.email());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authentication
    ) {
        log.info("Token refresh attempt for token: {}", authentication);
        TokenResponse response = authService.refreshToken(authentication);
        log.info("Token refreshed successfully for token: {}", authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authentication
    ) {
        log.info("Logout attempt for token: {}", authentication);
        authService.logout(authentication);
        log.info("Logout successful for token: {}", authentication);
        return ResponseEntity.noContent().build();
    }
}
