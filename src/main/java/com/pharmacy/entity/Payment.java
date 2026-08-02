package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    
    @Column(nullable = false, length = 100)
    private String razorpayOrderId;
    
    @Column(length = 100)
    private String razorpayPaymentId;
    
    @Column(length = 500)
    private String razorpaySignature;
    
    @Column(nullable = false, length = 100)
    private String transactionId;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String paymentStatus = "PENDING";
    
    @Column(nullable = false, length = 50)
    private String paymentMethod;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";
    
    @Column
    private LocalDateTime transactionTime;
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String paymentGateway = "RAZORPAY";
    
    @Column(length = 500)
    private String errorMessage;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
