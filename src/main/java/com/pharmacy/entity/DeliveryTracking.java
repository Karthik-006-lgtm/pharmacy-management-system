package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id", nullable = false)
    private User pharmacist;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TrackingStatus currentStatus = TrackingStatus.ORDER_ACCEPTED;
    
    @Column
    private Double estimatedDistance;
    
    @Column
    private Integer estimatedTimeMinutes;
    
    @Column
    private LocalDateTime estimatedArrival;
    
    @Column
    private LocalDateTime orderAcceptedAt;
    
    @Column
    private LocalDateTime preparingAt;
    
    @Column
    private LocalDateTime packedAt;
    
    @Column
    private LocalDateTime outForDeliveryAt;
    
    @Column
    private LocalDateTime nearCustomerAt;
    
    @Column
    private LocalDateTime deliveredAt;
    
    @Column(length = 500)
    private String currentLocation;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        orderAcceptedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TrackingStatus {
        ORDER_ACCEPTED,
        PREPARING_MEDICINES,
        PACKED,
        OUT_FOR_DELIVERY,
        NEAR_CUSTOMER,
        DELIVERED
    }
}
