package com.pharmacy.service;

import com.pharmacy.entity.DeliveryDelay;
import com.pharmacy.entity.DeliveryTracking;
import com.pharmacy.entity.Notification;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.repository.DeliveryDelayRepository;
import com.pharmacy.repository.DeliveryTrackingRepository;
import com.pharmacy.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryDelayService {
    
    private final DeliveryDelayRepository deliveryDelayRepository;
    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    
    public DeliveryDelayService(DeliveryDelayRepository deliveryDelayRepository,
                               DeliveryTrackingRepository deliveryTrackingRepository,
                               OrderRepository orderRepository,
                               NotificationService notificationService) {
        this.deliveryDelayRepository = deliveryDelayRepository;
        this.deliveryTrackingRepository = deliveryTrackingRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public DeliveryDelay reportDelay(Long orderId, User pharmacist, 
                                    DeliveryDelay.DelayReason reason,
                                    String customReason, Integer delayMinutes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        DeliveryTracking tracking = deliveryTrackingRepository.findByOrder(order)
                .orElseThrow(() -> new RuntimeException("Delivery tracking not found"));
        
        LocalDateTime originalEta = tracking.getEstimatedArrival();
        LocalDateTime updatedEta = LocalDateTime.now().plusMinutes(delayMinutes);
        
        DeliveryDelay delay = DeliveryDelay.builder()
                .order(order)
                .pharmacist(pharmacist)
                .reason(reason)
                .customReason(customReason)
                .delayMinutes(delayMinutes)
                .originalEta(originalEta)
                .updatedEta(updatedEta)
                .build();
        
        DeliveryDelay savedDelay = deliveryDelayRepository.save(delay);
        
        tracking.setEstimatedArrival(updatedEta);
        tracking.setEstimatedTimeMinutes(tracking.getEstimatedTimeMinutes() + delayMinutes);
        deliveryTrackingRepository.save(tracking);
        
        String reasonText = reason == DeliveryDelay.DelayReason.OTHER ? 
                customReason : formatDelayReason(reason);
        
        String message = String.format(
                "Your order #%s delivery is delayed by %d minutes. Reason: %s. New estimated arrival: %s",
                order.getOrderNumber(),
                delayMinutes,
                reasonText,
                formatTime(updatedEta)
        );
        
        notificationService.createNotification(
                order.getUser(),
                Notification.NotificationType.DELIVERY_DELAY,
                "Delivery Delayed",
                message,
                "Order",
                order.getId()
        );
        
        return savedDelay;
    }
    
    public List<DeliveryDelay> getOrderDelays(Long orderId) {
        return deliveryDelayRepository.findByOrderIdOrderByReportedAtDesc(orderId);
    }
    
    public List<DeliveryDelay> getPharmacistDelays(Long pharmacistId) {
        return deliveryDelayRepository.findByPharmacistId(pharmacistId);
    }
    
    private String formatDelayReason(DeliveryDelay.DelayReason reason) {
        switch (reason) {
            case TRAFFIC: return "Heavy Traffic";
            case RAIN: return "Rain";
            case ROAD_BLOCK: return "Road Block";
            case VEHICLE_ISSUE: return "Vehicle Issue";
            case MEDICAL_EMERGENCY: return "Medical Emergency";
            case PERSONAL_EMERGENCY: return "Personal Emergency";
            default: return "Other";
        }
    }
    
    private String formatTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime().toString().substring(0, 5);
    }
}
