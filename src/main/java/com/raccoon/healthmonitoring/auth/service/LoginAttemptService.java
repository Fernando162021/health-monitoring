package com.raccoon.healthmonitoring.auth.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 15;

    // Map: email -> LoginAttemptInfo
    private final Map<String, LoginAttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String email) {
        attemptsCache.remove(email);
    }

    public void loginFailed(String email) {
        LoginAttemptInfo info = attemptsCache.getOrDefault(email, new LoginAttemptInfo());
        info.incrementAttempts();

        if (info.getAttempts() >= MAX_ATTEMPTS) {
            info.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
        }

        attemptsCache.put(email, info);
    }

    public boolean isBlocked(String email) {
        LoginAttemptInfo info = attemptsCache.get(email);

        if (info == null) {
            return false;
        }

        // If is locked, check if lock time has expired
        if (info.getLockedUntil() != null) {
            if (LocalDateTime.now().isBefore(info.getLockedUntil())) {
                return true;
            } else {
                // Lock time expired, reset attempts
                attemptsCache.remove(email);
                return false;
            }
        }

        return false;
    }

    public int getRemainingAttempts(String email) {
        LoginAttemptInfo info = attemptsCache.get(email);
        if (info == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - info.getAttempts());
    }

    public LocalDateTime getLockedUntil(String email) {
        LoginAttemptInfo info = attemptsCache.get(email);
        return info != null ? info.getLockedUntil() : null;
    }

    @Getter
    public static class LoginAttemptInfo {
        private int attempts = 0;
        @Setter
        private LocalDateTime lockedUntil;

        public void incrementAttempts() {
            attempts++;
        }
    }
}
