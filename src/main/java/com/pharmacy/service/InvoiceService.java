package com.pharmacy.service;

import com.pharmacy.entity.Invoice;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }
    
    @Transactional
    public Invoice generateInvoice(Order order, String paymentMethod) {
        String invoiceNumber = generateInvoiceNumber();
        
        BigDecimal subtotal = order.getTotalAmount();
        BigDecimal taxRate = new BigDecimal("0.18");
        BigDecimal taxableAmount = subtotal.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.subtract(taxableAmount);
        
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .user(order.getUser())
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentStatus("PAID")
                .subtotal(taxableAmount)
                .tax(tax)
                .totalAmount(subtotal)
                .build();
        
        return invoiceRepository.save(invoice);
    }
    
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }
    
    public Invoice findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }
    
    public Invoice findByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElse(null);
    }
    
    public List<Invoice> getUserInvoices(User user) {
        return invoiceRepository.findByUserId(user.getId());
    }
    
    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "INV" + timestamp;
    }
}
