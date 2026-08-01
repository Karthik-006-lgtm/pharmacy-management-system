package com.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String cardNumber;
    
    private String cardHolderName;
    
    private String expiryDate;
    
    private String cvv;
    
    private String bankName;
    
    private String upiId;
    
    private String transactionId;
}
