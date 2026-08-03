package com.pharmacy.service;

import com.pharmacy.entity.Notification;
import com.pharmacy.entity.PasswordResetToken;
import com.pharmacy.entity.User;
import com.pharmacy.repository.PasswordResetTokenRepository;
import com.pharmacy.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;
    
    private static final int TOKEN_VALIDITY_MINUTES = 15;
    private static final int MAX_REQUESTS_PER_HOUR = 3;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    public PasswordResetService(UserRepository userRepository,
                               PasswordResetTokenRepository tokenRepository,
                               EmailService emailService,
                               NotificationService notificationService,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            // Log but don't reveal user existence for security
            log.warn("Password reset requested for non-existent email: {}", email);
            // Still return success to prevent email enumeration attacks
            return;
        }
        
        User user = userOptional.get();
        
        // Check rate limiting
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = tokenRepository.countRecentTokensByUser(user, oneHourAgo);
        
        if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
            throw new RuntimeException("Too many reset requests. Please try again later.");
        }
        
        // Generate secure token
        String token = generateSecureToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES);
        
        // Invalidate ALL existing tokens for this user (active or not)
        // This ensures only the NEWEST token works
        tokenRepository.invalidateAllUserTokens(user);
        
        // Create new token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .used(false)
                .build();
        
        tokenRepository.save(resetToken);
        
        // Send email
        sendPasswordResetEmail(user, token);
        
        // Create notification
        notificationService.createNotification(
                user,
                Notification.NotificationType.ORDER_STATUS_UPDATE,
                "Password Reset Requested",
                "A password reset link has been sent to your email. The link will expire in 15 minutes.",
                "PasswordReset",
                resetToken.getId()
        );
        
        log.info("Password reset token generated for user: {}", email);
    }
    
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    private void sendPasswordResetEmail(User user, String token) {
        try {
            String resetLink = baseUrl + "/reset-password?token=" + token;
            
            String htmlContent = buildPasswordResetEmailTemplate(user, resetLink, token);
            
            emailService.sendHtmlEmail(
                    user.getEmail(),
                    "Password Reset Request - Online Pharmacy Management System",
                    htmlContent,
                    "PASSWORD_RESET_REQUEST",
                    user.getId(),
                    null
            );
            
            log.info("Password reset email sent successfully to: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {} | Error: {}", 
                     user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }
    
    private String buildPasswordResetEmailTemplate(User user, String resetLink, String token) {
        String expiryTime = LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES)
                .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<style>" +
               "body{font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background-color:#f5f5f5;margin:0;padding:0;}" +
               ".container{max-width:600px;margin:30px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);}" +
               ".header{background:linear-gradient(135deg,#0d6efd 0%,#0a58ca 100%);color:#ffffff;padding:30px 20px;text-align:center;}" +
               ".header h1{margin:0;font-size:28px;font-weight:600;}" +
               ".logo{width:60px;height:60px;margin:0 auto 15px;background:#ffffff;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:30px;}" +
               ".content{padding:40px 30px;}" +
               ".greeting{font-size:18px;color:#333;margin-bottom:20px;font-weight:600;}" +
               ".message{font-size:15px;line-height:1.8;color:#555;margin-bottom:25px;}" +
               ".button-container{text-align:center;margin:35px 0;}" +
               ".reset-button{display:inline-block;padding:16px 40px;background:linear-gradient(135deg,#0d6efd 0%,#0a58ca 100%);color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;font-size:16px;box-shadow:0 4px 15px rgba(13,110,253,0.3);transition:all 0.3s ease;}" +
               ".reset-button:hover{transform:translateY(-2px);box-shadow:0 6px 20px rgba(13,110,253,0.4);}" +
               ".alternate-link{background:#f8f9fa;padding:20px;border-radius:8px;margin:25px 0;border-left:4px solid #0d6efd;}" +
               ".alternate-link p{margin:0 0 10px 0;font-size:13px;color:#666;font-weight:600;}" +
               ".alternate-link a{color:#0d6efd;word-break:break-all;font-size:12px;text-decoration:none;}" +
               ".info-box{background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:20px;margin:25px 0;}" +
               ".info-box .icon{font-size:24px;margin-bottom:10px;}" +
               ".info-box p{margin:5px 0;font-size:14px;color:#856404;}" +
               ".info-box strong{color:#664d03;}" +
               ".security-warning{background:#f8d7da;border:1px solid #f5c2c7;border-radius:8px;padding:20px;margin:25px 0;}" +
               ".security-warning .icon{font-size:24px;margin-bottom:10px;color:#842029;}" +
               ".security-warning p{margin:5px 0;font-size:14px;color:#842029;}" +
               ".details-table{width:100%;margin:25px 0;background:#f8f9fa;border-radius:8px;overflow:hidden;}" +
               ".details-table tr{border-bottom:1px solid #dee2e6;}" +
               ".details-table tr:last-child{border-bottom:none;}" +
               ".details-table td{padding:15px 20px;font-size:14px;}" +
               ".details-table td:first-child{font-weight:600;color:#495057;width:40%;}" +
               ".details-table td:last-child{color:#212529;}" +
               ".footer{background:#f8f9fa;padding:30px 20px;text-align:center;border-top:1px solid #dee2e6;}" +
               ".footer p{margin:8px 0;font-size:13px;color:#6c757d;}" +
               ".footer a{color:#0d6efd;text-decoration:none;}" +
               ".footer a:hover{text-decoration:underline;}" +
               "</style></head><body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<div class='logo'>🏥</div>" +
               "<h1>Online Pharmacy Management System</h1>" +
               "</div>" +
               "<div class='content'>" +
               "<div class='greeting'>Dear " + user.getFullName() + ",</div>" +
               "<div class='message'>" +
               "We received a request to reset the password for your account. If you made this request, click the button below to create a new password." +
               "</div>" +
               "<div class='button-container'>" +
               "<a href='" + resetLink + "' class='reset-button'>Reset Your Password</a>" +
               "</div>" +
               "<div class='alternate-link'>" +
               "<p>If the button doesn't work, copy and paste this link into your browser:</p>" +
               "<a href='" + resetLink + "'>" + resetLink + "</a>" +
               "</div>" +
               "<div class='info-box'>" +
               "<div class='icon'>⏰</div>" +
               "<p><strong>Important:</strong> This link will expire in <strong>" + TOKEN_VALIDITY_MINUTES + " minutes</strong> at " + expiryTime + "</p>" +
               "<p>After expiration, you'll need to request a new password reset link.</p>" +
               "</div>" +
               "<table class='details-table'>" +
               "<tr><td>Account Email</td><td>" + user.getEmail() + "</td></tr>" +
               "<tr><td>Request Time</td><td>" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "</td></tr>" +
               "<tr><td>Valid Until</td><td>" + expiryTime + "</td></tr>" +
               "<tr><td>Token ID</td><td>" + token.substring(0, 8) + "...</td></tr>" +
               "</table>" +
               "<div class='security-warning'>" +
               "<div class='icon'>🔒</div>" +
               "<p><strong>Security Alert:</strong></p>" +
               "<p>• If you did NOT request this password reset, please ignore this email. Your password will remain unchanged.</p>" +
               "<p>• Never share your password or this reset link with anyone.</p>" +
               "<p>• Our team will never ask for your password via email.</p>" +
               "</div>" +
               "<div class='message' style='margin-top:30px;font-size:13px;color:#666;'>" +
               "If you're having trouble accessing your account or didn't request this reset, please contact our support team immediately." +
               "</div>" +
               "</div>" +
               "<div class='footer'>" +
               "<p><strong>&copy; 2026 Online Pharmacy Management System. All rights reserved.</strong></p>" +
               "<p>Need help? Contact us at <a href='mailto:support@pharmacymanagement.com'>support@pharmacymanagement.com</a></p>" +
               "<p style='margin-top:15px;color:#999;font-size:11px;'>This is an automated message. Please do not reply to this email.</p>" +
               "</div>" +
               "</div></body></html>";
    }
    
    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        
        if (resetToken.getUsed()) {
            throw new RuntimeException("This reset link has already been used");
        }
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("This reset link has expired. Please request a new one.");
        }
        
        return resetToken;
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = validateToken(token);
        User user = resetToken.getUser();
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        // Create notification
        notificationService.createNotification(
                user,
                Notification.NotificationType.ORDER_STATUS_UPDATE,
                "Password Changed Successfully",
                "Your password has been changed successfully. You can now login with your new password.",
                "PasswordReset",
                null
        );
        
        // Send confirmation email
        sendPasswordChangedEmail(user);
        
        log.info("Password successfully reset for user: {}", user.getEmail());
    }
    
    private void sendPasswordChangedEmail(User user) {
        try {
            String htmlContent = buildPasswordChangedEmailTemplate(user);
            
            emailService.sendHtmlEmail(
                    user.getEmail(),
                    "Password Changed Successfully - Online Pharmacy Management System",
                    htmlContent,
                    "PASSWORD_CHANGED",
                    user.getId(),
                    null
            );
            
            log.info("Password changed confirmation email sent to: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send password changed email to: {} | Error: {}", 
                     user.getEmail(), e.getMessage());
            // Don't throw exception as password has already been changed
        }
    }
    
    private String buildPasswordChangedEmailTemplate(User user) {
        String changeTime = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<style>" +
               "body{font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background-color:#f5f5f5;margin:0;padding:0;}" +
               ".container{max-width:600px;margin:30px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);}" +
               ".header{background:linear-gradient(135deg,#198754 0%,#146c43 100%);color:#ffffff;padding:30px 20px;text-align:center;}" +
               ".header h1{margin:0;font-size:28px;font-weight:600;}" +
               ".success-icon{width:80px;height:80px;margin:0 auto 15px;background:#ffffff;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:40px;}" +
               ".content{padding:40px 30px;}" +
               ".greeting{font-size:18px;color:#333;margin-bottom:20px;font-weight:600;}" +
               ".message{font-size:15px;line-height:1.8;color:#555;margin-bottom:25px;}" +
               ".success-box{background:#d1e7dd;border:1px solid #badbcc;border-radius:8px;padding:25px;margin:25px 0;text-align:center;}" +
               ".success-box .icon{font-size:50px;margin-bottom:15px;}" +
               ".success-box h2{margin:0 0 10px 0;color:#0f5132;font-size:22px;}" +
               ".success-box p{margin:5px 0;color:#0f5132;font-size:15px;}" +
               ".details-table{width:100%;margin:25px 0;background:#f8f9fa;border-radius:8px;overflow:hidden;}" +
               ".details-table tr{border-bottom:1px solid #dee2e6;}" +
               ".details-table tr:last-child{border-bottom:none;}" +
               ".details-table td{padding:15px 20px;font-size:14px;}" +
               ".details-table td:first-child{font-weight:600;color:#495057;width:40%;}" +
               ".details-table td:last-child{color:#212529;}" +
               ".warning-box{background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:20px;margin:25px 0;}" +
               ".warning-box .icon{font-size:28px;margin-bottom:10px;}" +
               ".warning-box p{margin:8px 0;font-size:14px;color:#664d03;}" +
               ".warning-box strong{color:#533c02;}" +
               ".action-button{display:inline-block;padding:14px 35px;background:linear-gradient(135deg,#198754 0%,#146c43 100%);color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;font-size:15px;margin:20px 0;box-shadow:0 4px 15px rgba(25,135,84,0.3);}" +
               ".footer{background:#f8f9fa;padding:30px 20px;text-align:center;border-top:1px solid #dee2e6;}" +
               ".footer p{margin:8px 0;font-size:13px;color:#6c757d;}" +
               ".footer a{color:#0d6efd;text-decoration:none;}" +
               ".footer a:hover{text-decoration:underline;}" +
               "</style></head><body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<div class='success-icon'>✓</div>" +
               "<h1>Password Changed Successfully</h1>" +
               "</div>" +
               "<div class='content'>" +
               "<div class='greeting'>Dear " + user.getFullName() + ",</div>" +
               "<div class='success-box'>" +
               "<div class='icon'>🔐</div>" +
               "<h2>Your Password Has Been Updated</h2>" +
               "<p>You can now login to your account using your new password.</p>" +
               "</div>" +
               "<div class='message'>" +
               "This email confirms that the password for your Online Pharmacy Management System account has been successfully changed." +
               "</div>" +
               "<table class='details-table'>" +
               "<tr><td>Account Email</td><td>" + user.getEmail() + "</td></tr>" +
               "<tr><td>Changed On</td><td>" + changeTime + "</td></tr>" +
               "<tr><td>Account Status</td><td><strong style='color:#198754;'>Active & Secure</strong></td></tr>" +
               "</table>" +
               "<div class='warning-box'>" +
               "<div class='icon'>⚠️</div>" +
               "<p><strong>Did you make this change?</strong></p>" +
               "<p>If you did NOT change your password, someone may have unauthorized access to your account.</p>" +
               "<p><strong>Please contact our support team immediately at support@pharmacymanagement.com</strong></p>" +
               "</div>" +
               "<div style='text-align:center;'>" +
               "<a href='" + baseUrl + "/login' class='action-button'>Login to Your Account</a>" +
               "</div>" +
               "<div class='message' style='margin-top:30px;font-size:14px;color:#666;'>" +
               "<strong>Security Tips:</strong><br>" +
               "• Use a strong, unique password for your account<br>" +
               "• Never share your password with anyone<br>" +
               "• Enable two-factor authentication if available<br>" +
               "• Be cautious of phishing emails<br>" +
               "</div>" +
               "</div>" +
               "<div class='footer'>" +
               "<p><strong>&copy; 2026 Online Pharmacy Management System. All rights reserved.</strong></p>" +
               "<p>Need help? Contact us at <a href='mailto:support@pharmacymanagement.com'>support@pharmacymanagement.com</a></p>" +
               "<p style='margin-top:15px;color:#999;font-size:11px;'>This is an automated security notification. Please do not reply to this email.</p>" +
               "</div>" +
               "</div></body></html>";
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Expired password reset tokens cleaned up");
    }
}
