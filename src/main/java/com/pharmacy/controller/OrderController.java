package com.pharmacy.controller;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.PrescriptionService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final com.pharmacy.service.DeliveryTrackingService deliveryTrackingService;
    private final com.pharmacy.service.DeliveryDelayService deliveryDelayService;
    
    public OrderController(OrderService orderService, PrescriptionService prescriptionService,
                           SecurityUtil securityUtil,
                           com.pharmacy.service.DeliveryTrackingService deliveryTrackingService,
                           com.pharmacy.service.DeliveryDelayService deliveryDelayService) {
        this.orderService = orderService;
        this.prescriptionService = prescriptionService;
        this.securityUtil = securityUtil;
        this.deliveryTrackingService = deliveryTrackingService;
        this.deliveryDelayService = deliveryDelayService;
    }
    
    @GetMapping("/checkout")
    public String checkout(Model model) {
        User currentUser = securityUtil.getCurrentUser();
        
        // Check if any cart item requires prescription
        boolean requiresPrescription = orderService.checkPrescriptionRequired(currentUser);
        
        model.addAttribute("user", currentUser);
        model.addAttribute("requiresPrescription", requiresPrescription);
        return "orders/checkout";
    }
    
    @PostMapping("/place")
    public String placeOrder(@RequestParam String paymentMethod,
                             @RequestParam(required = false) MultipartFile prescriptionFile,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            boolean requiresPrescription = orderService.checkPrescriptionRequired(currentUser);
            
            // Validate prescription upload if required
            if (requiresPrescription && (prescriptionFile == null || prescriptionFile.isEmpty())) {
                redirectAttributes.addFlashAttribute("error", "Prescription is required for this order!");
                return "redirect:/orders/checkout";
            }
            
            // Check if payment method is Cash on Delivery
            boolean isCOD = paymentMethod.equalsIgnoreCase("Cash on Delivery") || 
                           paymentMethod.equalsIgnoreCase("COD");
            
            // Create order
            Order order = orderService.createOrderWithPaymentAndPrescription(
                currentUser, paymentMethod, requiresPrescription);
            
            // Upload prescription if provided
            if (requiresPrescription && prescriptionFile != null && !prescriptionFile.isEmpty()) {
                prescriptionService.uploadPrescription(currentUser, order, prescriptionFile, null);
            }
            
            // For COD, redirect to confirmation directly
            if (isCOD) {
                redirectAttributes.addFlashAttribute("success", 
                    "Order placed successfully! Order Number: " + order.getOrderNumber());
                return "redirect:/orders/confirmation?orderId=" + order.getId();
            }
            
            // For online payments, redirect to Razorpay payment page
            model.addAttribute("order", order);
            model.addAttribute("paymentMethod", paymentMethod);
            return "redirect:/orders/payment-gateway?orderId=" + order.getId() + 
                   "&paymentMethod=" + paymentMethod;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
    
    @GetMapping("/payment-gateway")
    public String showPaymentGateway(@RequestParam Long orderId,
                                     @RequestParam String paymentMethod,
                                     Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        model.addAttribute("paymentMethod", paymentMethod);
        return "orders/razorpay-payment";
    }
    
    @GetMapping("/confirmation")
    public String orderConfirmation(@RequestParam Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "orders/confirmation";
    }
    
    @GetMapping("/payment/{orderId}")
    public String showPaymentPage(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "orders/payment";
    }
    
    @PostMapping("/complete-payment/{orderId}")
    public String completePayment(@PathVariable Long orderId,
                                  @RequestParam String paymentMethod,
                                  RedirectAttributes redirectAttributes) {
        try {
            orderService.completePayment(orderId);
            redirectAttributes.addFlashAttribute("success", "Payment completed successfully!");
            return "redirect:/orders/confirmation?orderId=" + orderId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/payment/" + orderId;
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
    
    @GetMapping("/track/{orderId}")
    public String trackOrderById(@PathVariable Long orderId, Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.getOrderById(orderId);
        
        try {
            com.pharmacy.entity.DeliveryTracking tracking = 
                    deliveryTrackingService.getTrackingByOrderId(orderId);
            java.util.List<com.pharmacy.entity.DeliveryDelay> delays = 
                    deliveryDelayService.getOrderDelays(orderId);
            
            model.addAttribute("order", order);
            model.addAttribute("tracking", tracking);
            model.addAttribute("delays", delays);
            return "orders/delivery-tracking";
        } catch (Exception e) {
            model.addAttribute("order", order);
            model.addAttribute("prescription", prescriptionService.findByOrderId(order.getId()));
            return "orders/track";
        }
    }
    
    @GetMapping("/feedback")
    public String feedbackForm(@RequestParam Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "orders/feedback";
    }
    
    @PostMapping("/upload-prescription")
    public String uploadPrescription(@RequestParam Long orderId,
                                     @RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            Order order = orderService.getOrderById(orderId);
            prescriptionService.uploadPrescription(currentUser, order, file, null);
            redirectAttributes.addFlashAttribute("success", "Prescription uploaded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload prescription: " + e.getMessage());
        }
        
        return "redirect:/orders";
    }
}
