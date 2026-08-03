package com.pharmacy.controller;

import com.pharmacy.entity.PasswordResetToken;
import com.pharmacy.service.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                       RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.initiatePasswordReset(email);
            redirectAttributes.addFlashAttribute("success", 
                "If an account exists with this email, a password reset link has been sent.");
            return "redirect:/forgot-password";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        } catch (Exception e) {
            log.error("Error processing forgot password request: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred. Please try again later.");
            return "redirect:/forgot-password";
        }
    }
    
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            PasswordResetToken resetToken = passwordResetService.validateToken(token);
            model.addAttribute("token", token);
            model.addAttribute("email", resetToken.getUser().getEmail());
            return "auth/reset-password";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        } catch (Exception e) {
            log.error("Error validating reset token: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "Invalid or expired reset link. Please request a new one.");
            return "redirect:/forgot-password";
        }
    }
    
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/reset-password?token=" + token;
            }
            
            // Validate password strength
            if (!isValidPassword(password)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Password must be at least 8 characters long and contain uppercase, lowercase, number, and special character");
                return "redirect:/reset-password?token=" + token;
            }
            
            // Reset password
            passwordResetService.resetPassword(token, password);
            
            redirectAttributes.addFlashAttribute("success", 
                "Password reset successful! You can now login with your new password.");
            return "redirect:/login";
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred. Please try again.");
            return "redirect:/reset-password?token=" + token;
        }
    }
    
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> 
            "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
}
