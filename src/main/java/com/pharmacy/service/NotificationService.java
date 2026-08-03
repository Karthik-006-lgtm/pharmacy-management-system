package com.pharmacy.service;

import com.pharmacy.entity.Notification;
import com.pharmacy.entity.User;
import com.pharmacy.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }
    
    @Transactional
    public Notification createNotification(User user, Notification.NotificationType type, 
                                          String title, String message,
                                          String entityType, Long entityId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send email notification asynchronously
        sendEmailNotification(user, type, title, message, entityType, entityId);
        
        return savedNotification;
    }
    
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }
    
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }
    
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        LocalDateTime now = LocalDateTime.now();
        
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });
        
        notificationRepository.saveAll(unreadNotifications);
    }
    
    private void sendEmailNotification(User user, Notification.NotificationType type, 
                                      String title, String message,
                                      String entityType, Long entityId) {
        try {
            String emailSubject = getEmailSubject(type, title);
            String emailContent = buildEmailContent(type, message, user);
            Map<String, String> details = buildEmailDetails(type, entityType, entityId);
            
            String htmlContent = emailService.buildEmailTemplate(title, emailContent, details);
            
            Long orderId = "Order".equals(entityType) ? entityId : null;
            emailService.sendHtmlEmail(
                user.getEmail(), 
                emailSubject, 
                htmlContent, 
                type.name(), 
                user.getId(), 
                orderId
            );
        } catch (Exception e) {
            // Log but don't fail the transaction
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }
    
    private String getEmailSubject(Notification.NotificationType type, String title) {
        return switch (type) {
            case PAYMENT_SUCCESS -> "Payment Successful - " + title;
            case ORDER_PLACED -> "Order Placed Successfully - " + title;
            case ORDER_DELIVERED -> "Order Delivered - " + title;
            case INVOICE_GENERATED -> "Invoice Generated - " + title;
            case DELIVERY_STARTED -> "Your Order is Out for Delivery";
            case DELIVERY_DELAY -> "Delivery Delay Notification";
            case ORDER_STATUS_UPDATE -> "Order Status Update";
            case MEDICINE_REQUEST -> "New Medicine Request";
            case REQUEST_ACCEPTED -> "Medicine Request Accepted";
            case REQUEST_REJECTED -> "Medicine Request Rejected";
            case PRESCRIPTION_UPLOADED -> "Prescription Uploaded";
            default -> title;
        };
    }
    
    private String buildEmailContent(Notification.NotificationType type, String message, User user) {
        return "Dear " + user.getFullName() + ",<br><br>" + message + "<br><br>Thank you for choosing our pharmacy!";
    }
    
    private Map<String, String> buildEmailDetails(Notification.NotificationType type, String entityType, Long entityId) {
        Map<String, String> details = new HashMap<>();
        if (entityType != null && entityId != null) {
            details.put(entityType + " ID", String.valueOf(entityId));
        }
        details.put("Notification Type", type.name().replace("_", " "));
        return details;
    }
}
