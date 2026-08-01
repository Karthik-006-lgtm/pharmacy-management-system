package com.pharmacy.service;

import com.pharmacy.entity.DeliveryTracking;
import com.pharmacy.entity.Notification;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.DeliveryTrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryTrackingService {
    
    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final NotificationService notificationService;
    
    public DeliveryTrackingService(DeliveryTrackingRepository deliveryTrackingRepository,
                                  NotificationService notificationService) {
        this.deliveryTrackingRepository = deliveryTrackingRepository;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public DeliveryTracking initializeTracking(Order order, User pharmacist) {
        Double estimatedDistance = calculateEstimatedDistance(order, pharmacist);
        Integer estimatedTime = calculateEstimatedTime(estimatedDistance);
        
        DeliveryTracking tracking = DeliveryTracking.builder()
                .order(order)
                .pharmacist(pharmacist)
                .currentStatus(DeliveryTracking.TrackingStatus.ORDER_ACCEPTED)
                .estimatedDistance(estimatedDistance)
                .estimatedTimeMinutes(estimatedTime)
                .estimatedArrival(LocalDateTime.now().plusMinutes(estimatedTime))
                .currentLocation(pharmacist.getCity())
                .build();
        
        DeliveryTracking savedTracking = deliveryTrackingRepository.save(tracking);
        
        notificationService.createNotification(
                order.getUser(),
                Notification.NotificationType.DELIVERY_STARTED,
                "Delivery Started",
                String.format("Your order #%s is being prepared. Estimated delivery in %d minutes.",
                        order.getOrderNumber(), estimatedTime),
                "Order",
                order.getId()
        );
        
        return savedTracking;
    }
    
    @Transactional
    public DeliveryTracking updateTrackingStatus(Long trackingId, 
                                                DeliveryTracking.TrackingStatus newStatus) {
        DeliveryTracking tracking = deliveryTrackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery tracking not found"));
        
        tracking.setCurrentStatus(newStatus);
        
        switch (newStatus) {
            case PREPARING_MEDICINES:
                tracking.setPreparingAt(LocalDateTime.now());
                sendStatusNotification(tracking, "Preparing Medicines", 
                        "Your medicines are being prepared.");
                break;
            case PACKED:
                tracking.setPackedAt(LocalDateTime.now());
                sendStatusNotification(tracking, "Order Packed", 
                        "Your order has been packed and ready for delivery.");
                break;
            case OUT_FOR_DELIVERY:
                tracking.setOutForDeliveryAt(LocalDateTime.now());
                sendStatusNotification(tracking, "Out for Delivery", 
                        "Your order is out for delivery.");
                break;
            case NEAR_CUSTOMER:
                tracking.setNearCustomerAt(LocalDateTime.now());
                sendStatusNotification(tracking, "Delivery Agent Nearby", 
                        "Your delivery agent is nearby. Please be ready to receive your order.");
                break;
            case DELIVERED:
                tracking.setDeliveredAt(LocalDateTime.now());
                sendStatusNotification(tracking, "Order Delivered", 
                        "Your order has been delivered. Please provide feedback.");
                break;
        }
        
        return deliveryTrackingRepository.save(tracking);
    }
    
    @Transactional
    public DeliveryTracking updateTrackingStatusByOrderId(Long orderId, 
                                                         DeliveryTracking.TrackingStatus newStatus) {
        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery tracking not found for order"));
        
        return updateTrackingStatus(tracking.getId(), newStatus);
    }
    
    private void sendStatusNotification(DeliveryTracking tracking, String title, String message) {
        notificationService.createNotification(
                tracking.getOrder().getUser(),
                Notification.NotificationType.ORDER_STATUS_UPDATE,
                title,
                String.format("%s Order #%s", message, tracking.getOrder().getOrderNumber()),
                "Order",
                tracking.getOrder().getId()
        );
    }
    
    public DeliveryTracking getTrackingByOrderId(Long orderId) {
        return deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery tracking not found"));
    }
    
    public List<DeliveryTracking> getPharmacistTrackings(Long pharmacistId) {
        return deliveryTrackingRepository.findByPharmacistId(pharmacistId);
    }
    
    private Double calculateEstimatedDistance(Order order, User pharmacist) {
        if (order.getDeliveryLatitude() != null && order.getDeliveryLongitude() != null &&
            pharmacist.getLatitude() != null && pharmacist.getLongitude() != null) {
            
            return calculateDistance(
                    pharmacist.getLatitude(), pharmacist.getLongitude(),
                    order.getDeliveryLatitude(), order.getDeliveryLongitude()
            );
        }
        
        return 5.0;
    }
    
    private Integer calculateEstimatedTime(Double distanceKm) {
        final double AVERAGE_SPEED_KMH = 30.0;
        double hours = distanceKm / AVERAGE_SPEED_KMH;
        int minutes = (int) Math.ceil(hours * 60);
        
        return Math.max(minutes + 10, 20);
    }
    
    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
