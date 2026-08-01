package com.pharmacy.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryFeedbackDto {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Arrived on time field is required")
    private Boolean arrivedOnTime;
    
    @NotNull(message = "Medicine delivered safely field is required")
    private Boolean medicineDeliveredSafely;
    
    @NotNull(message = "Pharmacist service rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer pharmacistServiceRating;
    
    @NotNull(message = "Delivery experience rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer deliveryExperienceRating;
    
    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer overallRating;
    
    @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
    private String comments;
}
