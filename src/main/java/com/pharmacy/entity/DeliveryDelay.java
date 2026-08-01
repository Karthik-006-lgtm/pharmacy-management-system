package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_delays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDelay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id", nullable = false)
    private User pharmacist;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DelayReason reason;
    
    @Column(length = 500)
    private String customReason;
    
    @Column(nullable = false)
    private Integer delayMinutes;
    
    @Column
    private LocalDateTime originalEta;
    
    @Column
    private LocalDateTime updatedEta;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime reportedAt;
    
    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
    }
    
    public enum DelayReason {
        TRAFFIC,
        RAIN,
        ROAD_BLOCK,
        VEHICLE_ISSUE,
        MEDICAL_EMERGENCY,
        PERSONAL_EMERGENCY,
        OTHER
    }
}
