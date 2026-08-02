package com.pharmacy.controller;

import com.pharmacy.dto.PaymentRequestDto;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.Payment;
import com.pharmacy.entity.User;
import com.pharmacy.service.InvoiceService;
import com.pharmacy.service.NotificationService;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.PaymentService;
import com.pharmacy.entity.Notification;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentApiController {
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;
    private final com.pharmacy.service.UserService userService;
    
    public PaymentApiController(PaymentService paymentService, OrderService orderService,
                               InvoiceService invoiceService, NotificationService notificationService,
                               com.pharmacy.service.UserService userService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
        this.userService = userService;
    }
    
    /**
     * Creates a Razorpay order for online payment.
     * This endpoint is called when user selects an online payment method.
     */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(
            @RequestBody Map<String, Object> requestData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Long orderId = Long.valueOf(requestData.get("orderId").toString());
            String paymentMethod = requestData.get("paymentMethod").toString();
            
            Order order = orderService.findById(orderId);
            
            // Verify order belongs to user
            if (!order.getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Unauthorized access to order");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create Razorpay order
            Map<String, Object> razorpayOrder = paymentService.createRazorpayOrder(order, paymentMethod);
            
            response.put("success", true);
            response.put("orderId", razorpayOrder.get("orderId"));
            response.put("amount", razorpayOrder.get("amount"));
            response.put("currency", razorpayOrder.get("currency"));
            response.put("key", razorpayOrder.get("key"));
            response.put("name", razorpayOrder.get("name"));
            response.put("description", razorpayOrder.get("description"));
            response.put("orderNumber", razorpayOrder.get("orderNumber"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create payment order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Verifies Razorpay payment and completes the order.
     * CRITICAL: This is the backend verification - never trust frontend success alone.
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestBody Map<String, String> paymentData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String razorpayOrderId = paymentData.get("razorpay_order_id");
            String razorpayPaymentId = paymentData.get("razorpay_payment_id");
            String razorpaySignature = paymentData.get("razorpay_signature");
            
            // Verify signature
            boolean isValid = paymentService.verifyPaymentSignature(
                    razorpayOrderId, razorpayPaymentId, razorpaySignature);
            
            if (isValid) {
                // Get payment and order details
                Payment payment = paymentService.getPaymentByOrderId(
                        Long.valueOf(paymentData.get("order_id")));
                Order order = payment.getOrder();
                
                // Generate invoice immediately for online payments
                invoiceService.generateInvoice(order, payment.getPaymentMethod());
                
                // Send notifications
                notificationService.createNotification(
                        order.getUser(),
                        Notification.NotificationType.PAYMENT_SUCCESS,
                        "Payment Successful",
                        String.format("Payment for order #%s completed successfully. Invoice generated.", 
                                order.getOrderNumber()),
                        "Order",
                        order.getId()
                );
                
                notificationService.createNotification(
                        order.getUser(),
                        Notification.NotificationType.ORDER_STATUS_UPDATE,
                        "Order Confirmed",
                        String.format("Your order #%s has been confirmed and will be processed soon.", 
                                order.getOrderNumber()),
                        "Order",
                        order.getId()
                );
                
                response.put("success", true);
                response.put("message", "Payment verified successfully");
                response.put("orderNumber", order.getOrderNumber());
                response.put("transactionId", payment.getTransactionId());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Payment verification failed");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Payment verification error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Handles payment failure scenarios.
     */
    @PostMapping("/failed")
    public ResponseEntity<Map<String, Object>> handlePaymentFailure(
            @RequestBody Map<String, String> failureData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String razorpayOrderId = failureData.get("razorpay_order_id");
            String errorMessage = failureData.get("error_message");
            
            paymentService.handlePaymentFailure(razorpayOrderId, errorMessage);
            
            response.put("success", true);
            response.put("message", "Payment failure recorded");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error recording failure: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * DEPRECATED: Legacy payment processing endpoint.
     * Use /create-order and /verify for online payments.
     * This is only kept for COD compatibility.
     */
    @Deprecated
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(
            @Valid @RequestBody PaymentRequestDto paymentRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Order order = orderService.findById(paymentRequest.getOrderId());
            
            if (!order.getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Unauthorized access to order");
                return ResponseEntity.badRequest().body(response);
            }
            
            String paymentDetails = buildPaymentDetails(paymentRequest);
            String transactionId = paymentService.processPayment(
                    order, paymentRequest.getPaymentMethod(), paymentDetails);
            
            invoiceService.generateInvoice(order, paymentRequest.getPaymentMethod());
            
            response.put("success", true);
            response.put("message", "Payment processed successfully");
            response.put("transactionId", transactionId);
            response.put("orderNumber", order.getOrderNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Payment processing failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/validate-card")
    public ResponseEntity<Map<String, Object>> validateCard(@RequestBody Map<String, String> cardDetails) {
        Map<String, Object> response = new HashMap<>();
        
        boolean isValid = paymentService.validateCardNumber(cardDetails.get("cardNumber")) &&
                         paymentService.validateExpiryDate(cardDetails.get("expiryDate")) &&
                         paymentService.validateCVV(cardDetails.get("cvv"));
        
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }
    
    private String buildPaymentDetails(PaymentRequestDto request) {
        StringBuilder details = new StringBuilder();
        details.append("Method: ").append(request.getPaymentMethod());
        
        if (request.getCardNumber() != null) {
            details.append(", Card: ****").append(request.getCardNumber().substring(
                    Math.max(0, request.getCardNumber().length() - 4)));
        }
        
        if (request.getBankName() != null) {
            details.append(", Bank: ").append(request.getBankName());
        }
        
        if (request.getUpiId() != null) {
            details.append(", UPI: ").append(request.getUpiId());
        }
        
        return details.toString();
    }
}
