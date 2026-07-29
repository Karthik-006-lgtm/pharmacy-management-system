package com.pharmacy.controller;

import com.pharmacy.entity.User;
import com.pharmacy.service.CartService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    
    private final CartService cartService;
    private final SecurityUtil securityUtil;
    
    public CartApiController(CartService cartService, SecurityUtil securityUtil) {
        this.cartService = cartService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getCartSummary() {
        try {
            User currentUser = securityUtil.getCurrentUser();
            int itemCount = cartService.getCartItemCount(currentUser);
            BigDecimal total = cartService.getCartTotal(currentUser);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("itemCount", itemCount);
            summary.put("subtotal", total.doubleValue());
            summary.put("total", total.doubleValue());
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "itemCount", 0,
                "subtotal", 0.0,
                "total", 0.0
            ));
        }
    }
}
