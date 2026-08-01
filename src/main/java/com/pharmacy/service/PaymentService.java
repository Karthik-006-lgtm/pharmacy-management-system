package com.pharmacy.service;

import com.pharmacy.entity.Order;
import com.pharmacy.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    
    private final OrderRepository orderRepository;
    
    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Transactional
    public String processPayment(Order order, String paymentMethod, String paymentDetails) {
        String transactionId = generateTransactionId(paymentMethod);
        
        boolean paymentSuccess = simulatePaymentProcessing(paymentMethod);
        
        if (paymentSuccess) {
            order.setPaymentStatus("PAID");
            order.setPaymentCompletedAt(LocalDateTime.now());
            order.setPaymentTransactionId(transactionId);
            orderRepository.save(order);
            
            return transactionId;
        } else {
            throw new RuntimeException("Payment processing failed");
        }
    }
    
    private boolean simulatePaymentProcessing(String paymentMethod) {
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
