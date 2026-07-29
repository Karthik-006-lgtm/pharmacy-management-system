package com.pharmacy.controller;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.PDFService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/invoice")
public class InvoiceController {
    
    private final OrderService orderService;
    private final PDFService pdfService;
    private final SecurityUtil securityUtil;
    
    public InvoiceController(OrderService orderService, PDFService pdfService, SecurityUtil securityUtil) {
        this.orderService = orderService;
        this.pdfService = pdfService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId) {
        User currentUser = securityUtil.getCurrentUser();
        Order order = orderService.findById(orderId);
        
        if (!order.getUser().getId().equals(currentUser.getId()) && 
            currentUser.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        
        byte[] pdfBytes = pdfService.generateInvoice(order);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "invoice-" + order.getOrderNumber() + ".txt");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
