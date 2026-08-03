package com.pharmacy.service;

import com.pharmacy.entity.EmailLog;
import com.pharmacy.repository.EmailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    
    @Value("${app.mail.from}")
    private String fromEmail;
    
    @Value("${app.mail.company}")
    private String companyName;
    
    @Value("${app.mail.support}")
    private String supportEmail;
    
    public EmailService(JavaMailSender mailSender, EmailLogRepository emailLogRepository) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
    }
    
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent, String emailType, Long userId, Long orderId) {
        EmailLog emailLog = EmailLog.builder()
                .recipient(to)
                .subject(subject)
                .emailType(emailType)
                .relatedUserId(userId)
                .relatedOrderId(orderId)
                .status(EmailLog.EmailStatus.PENDING)
                .build();
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, companyName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            emailLog.setStatus(EmailLog.EmailStatus.SENT);
            log.info("Email sent successfully to: {} | Subject: {} | Type: {}", to, subject, emailType);
            
        } catch (Exception e) {
            emailLog.setStatus(EmailLog.EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            log.error("Failed to send email to: {} | Subject: {} | Error: {}", to, subject, e.getMessage());
        } finally {
            emailLogRepository.save(emailLog);
        }
    }
    
    @Async
    public void sendEmailWithAttachment(String to, String subject, String htmlContent, 
                                       String emailType, Long userId, Long orderId,
                                       byte[] attachment, String attachmentName) {
        EmailLog emailLog = EmailLog.builder()
                .recipient(to)
                .subject(subject)
                .emailType(emailType)
                .relatedUserId(userId)
                .relatedOrderId(orderId)
                .status(EmailLog.EmailStatus.PENDING)
                .build();
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, companyName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            if (attachment != null && attachmentName != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            }
            
            mailSender.send(message);
            
            emailLog.setStatus(EmailLog.EmailStatus.SENT);
            log.info("Email with attachment sent successfully to: {} | Subject: {} | Type: {}", to, subject, emailType);
            
        } catch (Exception e) {
            emailLog.setStatus(EmailLog.EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            log.error("Failed to send email with attachment to: {} | Subject: {} | Error: {}", to, subject, e.getMessage());
        } finally {
            emailLogRepository.save(emailLog);
        }
    }
    
    public String buildEmailTemplate(String title, String content, Map<String, String> details) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;background-color:#f4f4f4;margin:0;padding:0;}");
        html.append(".container{max-width:600px;margin:20px auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1);}");
        html.append(".header{background:#0d6efd;color:#fff;padding:20px;text-align:center;}");
        html.append(".header h1{margin:0;font-size:24px;}");
        html.append(".content{padding:30px;}");
        html.append(".content p{line-height:1.6;color:#333;}");
        html.append(".details{background:#f8f9fa;padding:15px;border-radius:5px;margin:20px 0;}");
        html.append(".details table{width:100%;border-collapse:collapse;}");
        html.append(".details td{padding:8px;border-bottom:1px solid #dee2e6;}");
        html.append(".details td:first-child{font-weight:bold;color:#495057;width:40%;}");
        html.append(".button{display:inline-block;padding:12px 30px;background:#0d6efd;color:#fff;text-decoration:none;border-radius:5px;margin:20px 0;}");
        html.append(".footer{background:#f8f9fa;padding:20px;text-align:center;font-size:12px;color:#6c757d;}");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>").append(companyName).append("</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        html.append("<h2>").append(title).append("</h2>");
        html.append("<p>").append(content).append("</p>");
        
        // Details
        if (details != null && !details.isEmpty()) {
            html.append("<div class='details'>");
            html.append("<table>");
            for (Map.Entry<String, String> entry : details.entrySet()) {
                html.append("<tr>");
                html.append("<td>").append(entry.getKey()).append("</td>");
                html.append("<td>").append(entry.getValue()).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("</div>");
        }
        
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>&copy; 2026 ").append(companyName).append(". All rights reserved.</p>");
        html.append("<p>For support, contact us at <a href='mailto:").append(supportEmail).append("'>").append(supportEmail).append("</a></p>");
        html.append("</div>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }
}
