package com.pharmacy.controller;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.Prescription;
import com.pharmacy.entity.User;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.PrescriptionService;
import com.pharmacy.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pharmacist/orders")
public class PharmacistOrderController {
    
    private final OrderService orderService;
    private final UserService userService;
    private final PrescriptionService prescriptionService;
    
    public PharmacistOrderController(OrderService orderService, UserService userService, PrescriptionService prescriptionService) {
        this.orderService = orderService;
        this.userService = userService;
        this.prescriptionService = prescriptionService;
    }
    
    @GetMapping("/pending")
    public String pendingOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        List<Order> pendingOrders = orderService.getPharmacistPendingOrders(pharmacist.getId());
        
        model.addAttribute("orders", pendingOrders);
        model.addAttribute("pharmacist", pharmacist);
        model.addAttribute("pageTitle", "Pending Order Requests");
        return "pharmacist/orders/pending";
    }
    
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        Order order = orderService.getOrderById(id);
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        
        // Get prescription if exists
        Prescription prescription = null;
        if (order.getPrescriptionRequired()) {
            prescription = prescriptionService.getByOrderId(order.getId());
        }
        
        model.addAttribute("order", order);
        model.addAttribute("prescription", prescription);
        model.addAttribute("pharmacist", pharmacist);
        return "pharmacist/orders/view";
    }
    
    @PostMapping("/accept/{id}")
    public String acceptOrder(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        orderService.acceptOrderByPharmacist(id, pharmacist);
        
        redirectAttributes.addFlashAttribute("successMessage", "Order accepted successfully!");
        return "redirect:/pharmacist/orders/pending";
    }
    
    @PostMapping("/reject/{id}")
    public String rejectOrder(@PathVariable Long id,
                              @RequestParam String remarks,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        orderService.rejectOrderByPharmacist(id, remarks);
        
        redirectAttributes.addFlashAttribute("successMessage", "Order rejected!");
        return "redirect:/pharmacist/orders/pending";
    }
    
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        orderService.updateOrderStatus(id, Order.OrderStatus.valueOf(status));
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully!");
        return "redirect:/pharmacist/profile";
    }
}
