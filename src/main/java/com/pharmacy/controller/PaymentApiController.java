package com.pharmacy.controller;

import com.pharmacy.dto.PaymentRequestDto;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.service.InvoiceService;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.PaymentService;
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
    private final com.pharmacy.service.UserService userService;
    
    public PaymentApiController(PaymentService paymentService, OrderService orderService,
                               InvoiceService invoiceService, 
                               com.pharmacy.service.UserService userService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.invoiceService = invoiceService;
        this.userService = userService;
    }
    
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
