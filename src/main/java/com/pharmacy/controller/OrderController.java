package com.pharmacy.controller;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.PrescriptionService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    private final OrderService orderService;
    private final PrescriptionService prescriptionService;
    private final SecurityUtil securityUtil;
    
    public OrderController(OrderService orderService, PrescriptionService prescriptionService,
                           SecurityUtil securityUtil) {
        this.orderService = orderService;
        this.prescriptionService = prescriptionService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping("/checkout")
    public String checkout(Model model) {
        User currentUser = securityUtil.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "orders/checkout";
    }
    
    @PostMapping("/place")
    public String placeOrder(RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            Order order = orderService.createOrder(currentUser);
            redirectAttributes.addFlashAttribute("success", "Order placed successfully! Order Number: " + order.getOrderNumber());
            return "redirect:/orders/track?orderNumber=" + order.getOrderNumber();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
    
    @GetMapping
    public String orderHistory(Model model) {
        User currentUser = securityUtil.getCurrentUser();
        List<Order> orders = orderService.getUserOrders(currentUser);
        model.addAttribute("orders", orders);
        return "orders/history";
    }
    
    @GetMapping("/track")
    public String trackOrder(@RequestParam String orderNumber, Model model) {
        Order order = orderService.findByOrderNumber(orderNumber);
        model.addAttribute("order", order);
        model.addAttribute("prescription", prescriptionService.findByOrderId(order.getId()));
        return "orders/track";
    }
    
    @PostMapping("/upload-prescription")
    public String uploadPrescription(@RequestParam Long orderId,
                                     @RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            prescriptionService.uploadPrescription(currentUser, orderId, file);
            redirectAttributes.addFlashAttribute("success", "Prescription uploaded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload prescription: " + e.getMessage());
        }
        
        return "redirect:/orders";
    }
}
