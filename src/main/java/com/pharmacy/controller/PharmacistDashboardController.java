package com.pharmacy.controller;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.service.DeliveryFeedbackService;
import com.pharmacy.service.DeliveryTrackingService;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.NotificationService;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/pharmacist")
public class PharmacistDashboardController {
    
    private final UserService userService;
    private final MedicineService medicineService;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final DeliveryFeedbackService deliveryFeedbackService;
    private final DeliveryTrackingService deliveryTrackingService;
    
    public PharmacistDashboardController(UserService userService, MedicineService medicineService, 
                                        OrderService orderService, NotificationService notificationService,
                                        DeliveryFeedbackService deliveryFeedbackService,
                                        DeliveryTrackingService deliveryTrackingService) {
        this.userService = userService;
        this.medicineService = medicineService;
        this.orderService = orderService;
        this.notificationService = notificationService;
        this.deliveryFeedbackService = deliveryFeedbackService;
        this.deliveryTrackingService = deliveryTrackingService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        
        List<Order> pendingOrders = orderService.getPharmacistPendingOrders(pharmacist.getId());
        List<Order> acceptedOrders = orderService.getPharmacistAcceptedOrders(pharmacist.getId());
        long uploadedMedicines = medicineService.countByPharmacist(pharmacist.getId());
        Long unreadNotifications = notificationService.getUnreadCount(pharmacist.getId());
        
        Double averageRating = deliveryFeedbackService.getPharmacistAverageRating(pharmacist.getId());
        Long feedbackCount = deliveryFeedbackService.getPharmacistFeedbackCount(pharmacist.getId());
        
        long activeDeliveries = deliveryTrackingService.getPharmacistTrackings(pharmacist.getId()).stream()
                .filter(t -> t.getCurrentStatus() != com.pharmacy.entity.DeliveryTracking.TrackingStatus.DELIVERED)
                .count();
        
        model.addAttribute("pharmacist", pharmacist);
        model.addAttribute("pendingOrdersCount", pendingOrders.size());
        model.addAttribute("acceptedOrdersCount", acceptedOrders.size());
        model.addAttribute("uploadedMedicinesCount", uploadedMedicines);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
        model.addAttribute("feedbackCount", feedbackCount != null ? feedbackCount : 0L);
        model.addAttribute("activeDeliveries", activeDeliveries);
        
        return "pharmacist/dashboard";
    }
    
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        
        List<Order> acceptedOrders = orderService.getPharmacistAcceptedOrders(pharmacist.getId());
        List<Order> allOrders = orderService.getPharmacistAllOrders(pharmacist.getId());
        
        Double averageRating = deliveryFeedbackService.getPharmacistAverageRating(pharmacist.getId());
        Long feedbackCount = deliveryFeedbackService.getPharmacistFeedbackCount(pharmacist.getId());
        
        model.addAttribute("pharmacist", pharmacist);
        model.addAttribute("acceptedOrders", acceptedOrders);
        model.addAttribute("allOrders", allOrders);
        model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
        model.addAttribute("feedbackCount", feedbackCount != null ? feedbackCount : 0L);
        
        return "pharmacist/profile";
    }
}
