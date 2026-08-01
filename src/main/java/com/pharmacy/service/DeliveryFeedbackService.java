package com.pharmacy.service;

import com.pharmacy.entity.DeliveryFeedback;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.repository.DeliveryFeedbackRepository;
import com.pharmacy.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryFeedbackService {
    
    private final DeliveryFeedbackRepository deliveryFeedbackRepository;
    private final OrderRepository orderRepository;
    
    public DeliveryFeedbackService(DeliveryFeedbackRepository deliveryFeedbackRepository,
                                  OrderRepository orderRepository) {
        this.deliveryFeedbackRepository = deliveryFeedbackRepository;
        this.orderRepository = orderRepository;
    }
    
    @Transactional
    public DeliveryFeedback submitFeedback(Long orderId, User customer,
                                          Boolean arrivedOnTime,
                                          Boolean medicineDeliveredSafely,
                                          Integer pharmacistServiceRating,
                                          Integer deliveryExperienceRating,
                                          Integer overallRating,
                                          String comments) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getPharmacist() == null) {
            throw new RuntimeException("No pharmacist assigned to this order");
        }
        
        Optional<DeliveryFeedback> existingFeedback = deliveryFeedbackRepository.findByOrderId(orderId);
        if (existingFeedback.isPresent()) {
            throw new RuntimeException("Feedback already submitted for this order");
        }
        
        DeliveryFeedback feedback = DeliveryFeedback.builder()
                .order(order)
                .customer(customer)
                .pharmacist(order.getPharmacist())
                .arrivedOnTime(arrivedOnTime)
                .medicineDeliveredSafely(medicineDeliveredSafely)
                .pharmacistServiceRating(pharmacistServiceRating)
                .deliveryExperienceRating(deliveryExperienceRating)
                .overallRating(overallRating)
                .comments(comments)
                .build();
        
        return deliveryFeedbackRepository.save(feedback);
    }
    
    public Optional<DeliveryFeedback> getFeedbackByOrderId(Long orderId) {
        return deliveryFeedbackRepository.findByOrderId(orderId);
    }
    
    public List<DeliveryFeedback> getCustomerFeedbacks(Long customerId) {
        return deliveryFeedbackRepository.findByCustomerId(customerId);
    }
    
    public List<DeliveryFeedback> getPharmacistFeedbacks(Long pharmacistId) {
        return deliveryFeedbackRepository.findByPharmacistId(pharmacistId);
    }
    
    public Double getPharmacistAverageRating(Long pharmacistId) {
        Double average = deliveryFeedbackRepository.calculateAverageRatingForPharmacist(pharmacistId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }
    
    public Long getPharmacistFeedbackCount(Long pharmacistId) {
        return deliveryFeedbackRepository.countFeedbackForPharmacist(pharmacistId);
    }
}
