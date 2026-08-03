package com.pharmacy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenCleanupService {
    
    private final PasswordResetService passwordResetService;
    
    public TokenCleanupService(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    // Run every hour
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        passwordResetService.cleanupExpiredTokens();
    }
}
