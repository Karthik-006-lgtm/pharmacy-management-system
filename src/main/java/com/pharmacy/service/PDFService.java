package com.pharmacy.service;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.OrderItem;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PDFService {
    
    public byte[] generateInvoice(Order order) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            // Create simple text-based invoice (can be replaced with iText or similar library)
            StringBuilder invoice = new StringBuilder();
            invoice.append("=".repeat(60)).append("\n");
            invoice.append("                    PHARMACARE INVOICE\n");
            invoice.append("=".repeat(60)).append("\n\n");
            
            invoice.append("Invoice Number: INV-").append(order.getId()).append("\n");
            invoice.append("Order Number: ").append(order.getOrderNumber()).append("\n");
            invoice.append("Date: ").append(order.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))).append("\n\n");
            
            invoice.append("Customer Details:\n");
            invoice.append("-".repeat(60)).append("\n");
            invoice.append("Name: ").append(order.getUser().getFullName()).append("\n");
            invoice.append("Email: ").append(order.getUser().getEmail()).append("\n");
            invoice.append("Phone: ").append(order.getContactPhone()).append("\n");
            invoice.append("Address: ").append(order.getShippingAddress()).append("\n");
            invoice.append("         ").append(order.getShippingCity()).append(", ").append(order.getShippingState()).append(" - ").append(order.getShippingPincode()).append("\n\n");
            
            invoice.append("Order Details:\n");
            invoice.append("-".repeat(60)).append("\n");
            invoice.append(String.format("%-30s %10s %10s %12s\n", "Medicine", "Price", "Qty", "Subtotal"));
            invoice.append("-".repeat(60)).append("\n");
            
            for (OrderItem item : order.getOrderItems()) {
                invoice.append(String.format("%-30s %10.2f %10d %12.2f\n",
                        item.getMedicine().getName().substring(0, Math.min(30, item.getMedicine().getName().length())),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal()));
            }
            
            invoice.append("-".repeat(60)).append("\n");
            invoice.append(String.format("%52s %12.2f\n", "Subtotal:", order.getTotalAmount()));
            invoice.append(String.format("%52s %12.2f\n", "Tax (0%):", 0.00));
            invoice.append(String.format("%52s %12.2f\n", "Total:", order.getTotalAmount()));
            invoice.append("=".repeat(60)).append("\n\n");
            
            invoice.append("Payment Method: Cash on Delivery\n");
            invoice.append("Status: ").append(order.getStatus()).append("\n\n");
            
            invoice.append("Thank you for shopping with PharmaCare!\n");
            invoice.append("For queries, contact: support@pharmacare.com\n");
            
            outputStream.write(invoice.toString().getBytes());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice", e);
        }
        
        return outputStream.toByteArray();
    }
}
