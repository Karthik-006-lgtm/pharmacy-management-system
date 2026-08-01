package com.pharmacy.controller;

import com.pharmacy.entity.DeliveryDelay;
import com.pharmacy.entity.DeliveryTracking;
import com.pharmacy.entity.User;
import com.pharmacy.service.DeliveryDelayService;
import com.pharmacy.service.DeliveryTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery-tracking")
public class DeliveryTrackingApiController {
    
    private final DeliveryTrackingService deliveryTrackingService;
    private final DeliveryDelayService deliveryDelayService;
    private final com.pharmacy.service.UserService userService;
    
    public DeliveryTrackingApiController(DeliveryTrackingService deliveryTrackingService,
                                        DeliveryDelayService deliveryDelayService,
                                        com.pharmacy.service.UserService userService) {
        this.deliveryTrackingService = deliveryTrackingService;
        this.deliveryDelayService = deliveryDelayService;
        this.userService = userService;
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getTrackingDetails(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            DeliveryTracking tracking = deliveryTrackingService.getTrackingByOrderId(orderId);
            List<DeliveryDelay> delays = deliveryDelayService.getOrderDelays(orderId);
            
            response.put("success", true);
            response.put("tracking", tracking);
            response.put("delays", delays);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/{trackingId}/update-status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long trackingId,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            DeliveryTracking.TrackingStatus trackingStatus = 
                    DeliveryTracking.TrackingStatus.valueOf(status);
            
            DeliveryTracking tracking = deliveryTrackingService.updateTrackingStatus(
                    trackingId, trackingStatus);
            
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("tracking", tracking);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/order/{orderId}/report-delay")
    public ResponseEntity<Map<String, Object>> reportDelay(
            @PathVariable Long orderId,
            @RequestParam String reason,
            @RequestParam(required = false) String customReason,
            @RequestParam Integer delayMinutes,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User pharmacist = userService.findByEmail(userDetails.getUsername());
            DeliveryDelay.DelayReason delayReason = DeliveryDelay.DelayReason.valueOf(reason);
            
            DeliveryDelay delay = deliveryDelayService.reportDelay(
                    orderId, pharmacist, delayReason, customReason, delayMinutes);
            
            response.put("success", true);
            response.put("message", "Delay reported successfully");
            response.put("delay", delay);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
