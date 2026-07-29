package com.pharmacy.controller;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.service.MedicineService;
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
    
    public PharmacistDashboardController(UserService userService, MedicineService medicineService, OrderService orderService) {
        this.userService = userService;
        this.medicineService = medicineService;
        this.orderService = orderService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        
        // Get pharmacist statistics
        List<Order> pendingOrders = orderService.getPharmacistPendingOrders(pharmacist.getId());
        List<Order> acceptedOrders = orderService.getPharmacistAcceptedOrders(pharmacist.getId());
        long uploadedMedicines = medicineService.countByPharmacist(pharmacist.getId());
        
        model.addAttribute("pharmacist", pharmacist);
        model.addAttribute("pendingOrdersCount", pendingOrders.size());
        model.addAttribute("acceptedOrdersCount", acceptedOrders.size());
        model.addAttribute("uploadedMedicinesCount", uploadedMedicines);
        
        return "pharmacist/dashboard";
    }
    
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        
        List<Order> acceptedOrders = orderService.getPharmacistAcceptedOrders(pharmacist.getId());
        List<Order> allOrders = orderService.getPharmacistAllOrders(pharmacist.getId());
        
        model.addAttribute("pharmacist", pharmacist);
        model.addAttribute("acceptedOrders", acceptedOrders);
        model.addAttribute("allOrders", allOrders);
        
        return "pharmacist/profile";
    }
}
