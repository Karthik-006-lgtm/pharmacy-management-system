package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id", nullable = false)
    private User pharmacist;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean arrivedOnTime = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean medicineDeliveredSafely = true;
    
    @Column(nullable = false)
    private Integer pharmacistServiceRating;
    
    @Column(nullable = false)
    private Integer deliveryExperienceRating;
    
    @Column(nullable = false)
    private Integer overallRating;
    
    @Column(length = 1000)
    private String comments;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;
    
    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
