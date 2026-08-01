package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id")
    private User pharmacist;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean prescriptionRequired = false;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false, length = 500)
    private String shippingAddress;
    
    @Column(nullable = false, length = 100)
    private String shippingCity;
    
    @Column(nullable = false, length = 100)
    private String shippingState;
    
    @Column(nullable = false, length = 10)
    private String shippingPincode;
    
    @Column(nullable = false, length = 15)
    private String contactPhone;
    
    @Column(length = 50)
    private String paymentMethod;
    
    @Column(length = 20)
    @Builder.Default
    private String paymentStatus = "PENDING";
    
    @Column
    private LocalDateTime paymentCompletedAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;
    
    @Column
    private LocalDateTime deliveryDate;
    
    @Column(length = 500)
    private String remarks;
    
    @Column(length = 100)
    private String paymentTransactionId;
    
    @Column
    private Double deliveryLatitude;
    
    @Column
    private Double deliveryLongitude;
    
    @Column
    private Integer estimatedDeliveryMinutes;
    
    @Column
    private LocalDateTime estimatedDeliveryTime;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean broadcastToPharmacists = false;
    
    @Column
    private LocalDateTime broadcastAt;
    
    @Column
    private LocalDateTime acceptedAt;
    
    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }
    
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    
    public enum OrderStatus {
        PENDING,
        PRESCRIPTION_VERIFICATION,
        APPROVED,
        REJECTED,
        PACKED,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}
