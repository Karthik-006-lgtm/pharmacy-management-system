package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String requestNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_pharmacist_id")
    private User acceptedPharmacist;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(nullable = false)
    private String medicineName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(length = 500)
    private String customerAddress;
    
    @Column(length = 100)
    private String customerCity;
    
    @Column(length = 100)
    private String customerState;
    
    @Column(length = 10)
    private String customerPincode;
    
    @Column(length = 15)
    private String customerPhone;
    
    @Column
    private Double customerLatitude;
    
    @Column
    private Double customerLongitude;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RequestStatus status = RequestStatus.BROADCAST;
    
    @Column
    private LocalDateTime broadcastAt;
    
    @Column
    private LocalDateTime acceptedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        broadcastAt = LocalDateTime.now();
    }
    
    public enum RequestStatus {
        BROADCAST,
        ACCEPTED,
        PREPARING,
        PACKED,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED
    }
}
