package com.pharmacy.controller;

import com.pharmacy.entity.User;
import com.pharmacy.service.CartService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Map Demo Controller
 * Demonstrates Leaflet + OpenStreetMap integration
 * For verification and testing purposes only
 */
@Controller
@RequestMapping("/map-demo")
public class MapDemoController {
    
    private final CartService cartService;
    private final SecurityUtil securityUtil;
    
    public MapDemoController(CartService cartService, SecurityUtil securityUtil) {
        this.cartService = cartService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping
    public String showMapDemo(Model model) {
        model.addAttribute("title", "Map Integration Demo");
        
        // Add cart count for navbar
        try {
            User currentUser = securityUtil.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("cartCount", cartService.getCartItemCount(currentUser));
            }
        } catch (Exception e) {
            // User might not be logged in, ignore
            model.addAttribute("cartCount", 0);
        }
        
        return "map-demo";
    }
}
