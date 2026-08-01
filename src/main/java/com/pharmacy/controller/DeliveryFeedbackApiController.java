package com.pharmacy.controller;

import com.pharmacy.dto.DeliveryFeedbackDto;
import com.pharmacy.entity.DeliveryFeedback;
import com.pharmacy.entity.User;
import com.pharmacy.service.DeliveryFeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/delivery-feedback")
public class DeliveryFeedbackApiController {
    
    private final DeliveryFeedbackService deliveryFeedbackService;
    private final com.pharmacy.service.UserService userService;
    
    public DeliveryFeedbackApiController(DeliveryFeedbackService deliveryFeedbackService,
                                        com.pharmacy.service.UserService userService) {
        this.deliveryFeedbackService = deliveryFeedbackService;
        this.userService = userService;
    }
    
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @Valid @RequestBody DeliveryFeedbackDto feedbackDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User customer = userService.findByEmail(userDetails.getUsername());
            
            DeliveryFeedback feedback = deliveryFeedbackService.submitFeedback(
                    feedbackDto.getOrderId(),
                    customer,
                    feedbackDto.getArrivedOnTime(),
                    feedbackDto.getMedicineDeliveredSafely(),
                    feedbackDto.getPharmacistServiceRating(),
                    feedbackDto.getDeliveryExperienceRating(),
                    feedbackDto.getOverallRating(),
                    feedbackDto.getComments()
            );
            
            response.put("success", true);
            response.put("message", "Thank you for your feedback!");
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getFeedbackByOrder(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<DeliveryFeedback> feedback = deliveryFeedbackService.getFeedbackByOrderId(orderId);
        
        if (feedback.isPresent()) {
            response.put("success", true);
            response.put("feedback", feedback.get());
        } else {
            response.put("success", false);
            response.put("message", "No feedback found for this order");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pharmacist/{pharmacistId}/rating")
    public ResponseEntity<Map<String, Object>> getPharmacistRating(@PathVariable Long pharmacistId) {
        Map<String, Object> response = new HashMap<>();
        
        Double averageRating = deliveryFeedbackService.getPharmacistAverageRating(pharmacistId);
        Long feedbackCount = deliveryFeedbackService.getPharmacistFeedbackCount(pharmacistId);
        
        response.put("averageRating", averageRating);
        response.put("feedbackCount", feedbackCount);
        
        return ResponseEntity.ok(response);
    }
}
