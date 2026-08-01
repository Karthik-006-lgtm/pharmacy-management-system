package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column(length = 100)
    private String relatedEntityType;
    
    @Column
    private Long relatedEntityId;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    
    @Column
    private LocalDateTime readAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum NotificationType {
        MEDICINE_REQUEST,
        REQUEST_ACCEPTED,
        REQUEST_REJECTED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        DELIVERY_STARTED,
        DELIVERY_DELAY,
        NEAR_DELIVERY,
        ORDER_DELIVERED,
        FEEDBACK_PENDING,
        ORDER_STATUS_UPDATE
    }
}
