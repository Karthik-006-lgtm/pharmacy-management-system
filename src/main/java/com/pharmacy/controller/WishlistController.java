package com.pharmacy.controller;

import com.pharmacy.entity.User;
import com.pharmacy.entity.Wishlist;
import com.pharmacy.service.CartService;
import com.pharmacy.service.WishlistService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    
    private final WishlistService wishlistService;
    private final CartService cartService;
    private final SecurityUtil securityUtil;
    
    public WishlistController(WishlistService wishlistService, CartService cartService, SecurityUtil securityUtil) {
        this.wishlistService = wishlistService;
        this.cartService = cartService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping
    public String viewWishlist(Model model) {
        User currentUser = securityUtil.getCurrentUser();
        List<Wishlist> wishlistItems = wishlistService.getWishlistItems(currentUser);
        
        model.addAttribute("wishlistItems", wishlistItems);
        model.addAttribute("wishlistCount", wishlistItems.size());
        model.addAttribute("cartCount", cartService.getCartItemCount(currentUser));
        
        return "wishlist/view";
    }
    
    @PostMapping("/add")
    public String addToWishlist(@RequestParam Long medicineId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            wishlistService.addToWishlist(currentUser, medicineId);
            redirectAttributes.addFlashAttribute("success", "Added to wishlist");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/medicines";
    }
    
    @PostMapping("/remove")
    public String removeFromWishlist(@RequestParam Long wishlistId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            wishlistService.removeFromWishlist(wishlistId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Removed from wishlist");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/wishlist";
    }
    
    @PostMapping("/move-to-cart")
    public String moveToCart(@RequestParam Long wishlistId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            Wishlist wishlist = wishlistService.getWishlistItems(currentUser).stream()
                    .filter(w -> w.getId().equals(wishlistId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Wishlist item not found"));
            
            cartService.addToCart(currentUser, wishlist.getMedicine().getId(), 1);
            wishlistService.removeFromWishlist(wishlistId, currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Moved to cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/wishlist";
    }
}
