package com.pharmacy.service;

import com.pharmacy.entity.Cart;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.User;
import com.pharmacy.exception.InsufficientStockException;
import com.pharmacy.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    
    private final CartRepository cartRepository;
    private final MedicineService medicineService;
    
    public CartService(CartRepository cartRepository, MedicineService medicineService) {
        this.cartRepository = cartRepository;
        this.medicineService = medicineService;
    }
    
    @Transactional
    public void addToCart(User user, Long medicineId, int quantity) {
        Medicine medicine = medicineService.findById(medicineId);
        
        if (medicine.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock for " + medicine.getName());
        }
        
        cartRepository.findByUserIdAndMedicineId(user.getId(), medicineId)
                .ifPresentOrElse(
                        cart -> {
                            int newQuantity = cart.getQuantity() + quantity;
                            if (medicine.getStock() < newQuantity) {
                                throw new InsufficientStockException("Insufficient stock for " + medicine.getName());
                            }
                            cart.setQuantity(newQuantity);
                            cartRepository.save(cart);
                        },
                        () -> {
                            Cart cart = Cart.builder()
                                    .user(user)
                                    .medicine(medicine)
                                    .quantity(quantity)
                                    .price(medicine.getPrice())
                                    .build();
                            cartRepository.save(cart);
                        }
                );
    }
    
    public List<Cart> getCartItems(User user) {
        return cartRepository.findByUser(user);
    }
    
    @Transactional
    public void updateCartItemQuantity(Long cartId, int quantity, User user) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
        
        if (cart.getMedicine().getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock");
        }
        
        cart.setQuantity(quantity);
        cartRepository.save(cart);
    }
    
    @Transactional
    public void removeCartItem(Long cartId, User user) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
        
        cartRepository.delete(cart);
    }
    
    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }
    
    public BigDecimal getCartTotal(User user) {
        List<Cart> cartItems = getCartItems(user);
        return cartItems.stream()
                .map(Cart::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getCartItemCount(User user) {
        return cartRepository.countByUserId(user.getId());
    }
}
