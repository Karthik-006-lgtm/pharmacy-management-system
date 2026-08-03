package com.pharmacy.scheduler;

import com.pharmacy.service.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to cleanup expired password reset tokens
 * Runs daily at 2:00 AM to maintain database hygiene
 */
@Slf4j
@Component
public class PasswordResetTokenCleanupScheduler {
    
    private final PasswordResetService passwordResetService;
    
    public PasswordResetTokenCleanupScheduler(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    /**
     * Clean up expired password reset tokens daily at 2:00 AM
     * Cron: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired password reset tokens...");
        try {
            passwordResetService.cleanupExpiredTokens();
            log.info("Successfully completed password reset token cleanup");
        } catch (Exception e) {
            log.error("Error during password reset token cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Additional cleanup every 6 hours for production safety
     * Runs at 00:00, 06:00, 12:00, 18:00
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void periodicCleanup() {
        log.debug("Running periodic password reset token cleanup...");
        try {
            passwordResetService.cleanupExpiredTokens();
        } catch (Exception e) {
            log.error("Error during periodic token cleanup: {}", e.getMessage(), e);
        }
    }
}
