package com.pharmacy.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineDto {
    private Long id;
    
    @NotBlank(message = "Medicine name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;
    
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    private String categoryName;
    
    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    private String manufacturer;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;
    
    @DecimalMin(value = "0.00", message = "Tax percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Tax percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Invalid tax format")
    private BigDecimal taxPercentage;
    
    @Size(max = 50, message = "Batch number must not exceed 50 characters")
    private String batchNumber;
    
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
    
    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    @NotNull(message = "Manufacture date is required")
    @Past(message = "Manufacture date must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate manufactureDate;
    
    private Boolean prescriptionRequired;
    
    private String imageUrl;
    
    private Boolean active;
}
