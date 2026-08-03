package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String recipient;
    
    @Column(nullable = false, length = 300)
    private String subject;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus status;
    
    @Column(length = 1000)
    private String errorMessage;
    
    @Column
    private Long relatedUserId;
    
    @Column
    private Long relatedOrderId;
    
    @Column(length = 50)
    private String emailType;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum EmailStatus {
        SENT,
        FAILED,
        PENDING
    }
}
