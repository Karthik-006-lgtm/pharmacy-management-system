package com.pharmacy.service;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.Payment;
import com.pharmacy.repository.OrderRepository;
import com.pharmacy.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    @Value("${razorpay.currency}")
    private String currency;
    
    @Value("${razorpay.company.name}")
    private String companyName;
    
    public PaymentService(OrderRepository orderRepository, PaymentRepository paymentRepository,
                         RazorpayClient razorpayClient) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
    }
    
    /**
     * Creates a Razorpay order for online payment methods.
     * This method is called when customer selects online payment options.
     * 
     * @param order The order for which payment is being processed
     * @param paymentMethod The payment method selected by customer
     * @return Map containing Razorpay order details
     * @throws RazorpayException if order creation fails
     */
    @Transactional
    public Map<String, Object> createRazorpayOrder(Order order, String paymentMethod) throws RazorpayException {
        // Convert amount to paise (Razorpay expects amount in smallest currency unit)
        int amountInPaise = order.getTotalAmount().multiply(new BigDecimal("100")).intValue();
        
        // Create Razorpay order using Map to avoid JSONObject conflicts
        org.json.JSONObject orderRequest = new org.json.JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", order.getOrderNumber());
        
        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        
        // Save payment record
        String transactionId = generateTransactionId(paymentMethod);
        Payment payment = Payment.builder()
                .order(order)
                .razorpayOrderId(razorpayOrder.get("id"))
                .transactionId(transactionId)
                .paymentStatus("PENDING")
                .paymentMethod(paymentMethod)
                .amount(order.getTotalAmount())
                .currency(currency)
                .paymentGateway("RAZORPAY")
                .build();
        
        paymentRepository.save(payment);
        
        // Prepare response as Map
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", razorpayOrder.get("id"));
        response.put("amount", amountInPaise);
        response.put("currency", currency);
        response.put("key", razorpayKeyId);
        response.put("name", companyName);
        response.put("description", "Order #" + order.getOrderNumber());
        response.put("orderNumber", order.getOrderNumber());
        
        return response;
    }
    
    /**
     * Verifies Razorpay payment signature and updates payment status.
     * This is the critical security step - NEVER trust frontend success without verification.
     * 
     * @param razorpayOrderId The Razorpay order ID
     * @param razorpayPaymentId The Razorpay payment ID
     * @param razorpaySignature The signature to verify
     * @return true if verification successful, false otherwise
     */
    @Transactional
    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, 
                                         String razorpaySignature) {
        try {
            // Verify signature using Razorpay Utils
            org.json.JSONObject options = new org.json.JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);
            
            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            
            if (isValid) {
                // Update payment record
                Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));
                
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setPaymentStatus("SUCCESS");
                payment.setTransactionTime(LocalDateTime.now());
                
                paymentRepository.save(payment);
                
                // Update order
                Order order = payment.getOrder();
                order.setPaymentStatus("PAID");
                order.setPaymentCompletedAt(LocalDateTime.now());
                order.setPaymentTransactionId(payment.getTransactionId());
                orderRepository.save(order);
                
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            // Log error and mark payment as failed
            try {
                Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElse(null);
                if (payment != null) {
                    payment.setPaymentStatus("FAILED");
                    payment.setErrorMessage(e.getMessage());
                    paymentRepository.save(payment);
                }
            } catch (Exception ex) {
                // Log error
            }
            return false;
        }
    }
    
    /**
     * Handles payment failure scenarios.
     * 
     * @param razorpayOrderId The Razorpay order ID
     * @param errorMessage The error message
     */
    @Transactional
    public void handlePaymentFailure(String razorpayOrderId, String errorMessage) {
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElse(null);
        
        if (payment != null) {
            payment.setPaymentStatus("FAILED");
            payment.setErrorMessage(errorMessage);
            paymentRepository.save(payment);
        }
    }
    
    /**
     * Gets payment details by order ID.
     * 
     * @param orderId The order ID
     * @return Payment object or null
     */
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }
    
    @Transactional
    public String processPayment(Order order, String paymentMethod, String paymentDetails) {
        // For Cash on Delivery, use simulated processing
        if (isCashOnDelivery(paymentMethod)) {
            return processCODPayment(order, paymentMethod);
        }
        
        // For online payments, this method should not be called directly
        // Use createRazorpayOrder() and verifyPaymentSignature() instead
        throw new RuntimeException("For online payments, use Razorpay integration");
    }
    
    /**
     * Processes Cash on Delivery payment (simulation only).
     * COD payments are marked as PENDING and completed only after delivery.
     * 
     * @param order The order
     * @param paymentMethod Payment method (should be COD)
     * @return Transaction ID
     */
    private String processCODPayment(Order order, String paymentMethod) {
        String transactionId = generateTransactionId(paymentMethod);
        
        // For COD, payment status remains PENDING until delivery
        order.setPaymentStatus("PENDING");
        order.setPaymentTransactionId(transactionId);
        orderRepository.save(order);
        
        // Save COD payment record
        Payment payment = Payment.builder()
                .order(order)
                .razorpayOrderId("COD-" + order.getOrderNumber())
                .transactionId(transactionId)
                .paymentStatus("PENDING")
                .paymentMethod(paymentMethod)
                .amount(order.getTotalAmount())
                .currency(currency)
                .paymentGateway("COD")
                .build();
        
        paymentRepository.save(payment);
        
        return transactionId;
    }
    
    /**
     * Checks if payment method is Cash on Delivery.
     */
    private boolean isCashOnDelivery(String paymentMethod) {
        if (paymentMethod == null) return false;
        String method = paymentMethod.toUpperCase().trim();
        return method.equals("COD") || method.equals("CASH ON DELIVERY");
    }
    
    /**
     * DEPRECATED: No longer used for online payments.
     * Simulates payment processing for development/testing purposes.
     * 
     * PRODUCTION NOTE: This is a SIMULATION for development and testing.
     * Replace this method with actual payment gateway integration:
     * - For Razorpay: Use Razorpay SDK with proper API credentials
     * - For Stripe: Use Stripe SDK with proper API credentials
     * - For PayU: Use PayU SDK with proper merchant credentials
     * 
     * Integration points required:
     * 1. Payment gateway SDK dependency in pom.xml
     * 2. API keys in application.properties (use environment variables in production)
     * 3. Webhook endpoints for payment confirmation callbacks
     * 4. Error handling for payment failures, timeouts, and network issues
     * 5. Transaction verification and reconciliation
     * 
     * @param paymentMethod The payment method selected by the user
     * @return true for successful payment (currently always returns true in simulation)
     * @deprecated Use Razorpay integration methods instead
     */
    @Deprecated
    private boolean simulatePaymentProcessing(String paymentMethod) {
        // SIMULATION: Always returns success
        // In production, this should make actual API calls to payment gateway
        return true;
    }
    
    private String generateTransactionId(String paymentMethod) {
        String prefix = getPaymentPrefix(paymentMethod);
        return prefix + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
    
    private String getPaymentPrefix(String paymentMethod) {
        if (paymentMethod == null) return "TXN";
        
        switch (paymentMethod.toUpperCase()) {
            case "GOOGLE PAY":
            case "GOOGLEPAY":
                return "GPAY";
            case "PHONEPE":
            case "PHONE PE":
                return "PHPE";
            case "PAYTM":
                return "PAYT";
            case "CREDIT CARD":
            case "CREDITCARD":
                return "CC";
            case "DEBIT CARD":
            case "DEBITCARD":
                return "DC";
            case "NET BANKING":
            case "NETBANKING":
                return "NB";
            default:
                return "TXN";
        }
    }
    
    public boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        return cardNumber.matches("\\d+");
    }
    
    public boolean validateExpiryDate(String expiryDate) {
        if (expiryDate == null || !expiryDate.matches("\\d{2}/\\d{2}")) {
            return false;
        }
        
        return true;
    }
    
    public boolean validateCVV(String cvv) {
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            return false;
        }
        
        return true;
    }
}
