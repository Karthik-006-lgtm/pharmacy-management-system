package com.pharmacy.controller;

import com.pharmacy.entity.Cart;
import com.pharmacy.entity.User;
import com.pharmacy.service.CartService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    private final CartService cartService;
    private final SecurityUtil securityUtil;
    
    public CartController(CartService cartService, SecurityUtil securityUtil) {
        this.cartService = cartService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping
    public String viewCart(Model model) {
        User currentUser = securityUtil.getCurrentUser();
        List<Cart> cartItems = cartService.getCartItems(currentUser);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartService.getCartTotal(currentUser));
        model.addAttribute("cartCount", cartItems.size());
        
        return "cart/view";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Long medicineId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            cartService.addToCart(currentUser, medicineId, quantity);
            redirectAttributes.addFlashAttribute("success", "Medicine added to cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/medicines";
    }
    
    @PostMapping("/update")
    public String updateCartItem(@RequestParam Long cartId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            cartService.updateCartItemQuantity(cartId, quantity, currentUser);
            redirectAttributes.addFlashAttribute("success", "Cart updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeCartItem(@RequestParam Long cartId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            cartService.removeCartItem(cartId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cart";
    }
}
