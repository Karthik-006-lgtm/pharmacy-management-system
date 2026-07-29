package com.pharmacy.service;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.User;
import com.pharmacy.entity.Wishlist;
import com.pharmacy.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class WishlistService {
    
    private final WishlistRepository wishlistRepository;
    private final MedicineService medicineService;
    
    public WishlistService(WishlistRepository wishlistRepository, MedicineService medicineService) {
        this.wishlistRepository = wishlistRepository;
        this.medicineService = medicineService;
    }
    
    @Transactional
    public void addToWishlist(User user, Long medicineId) {
        if (wishlistRepository.existsByUserAndMedicineId(user, medicineId)) {
            throw new RuntimeException("Medicine already in wishlist");
        }
        
        Medicine medicine = medicineService.findById(medicineId);
        
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .medicine(medicine)
                .build();
        
        wishlistRepository.save(wishlist);
    }
    
    public List<Wishlist> getWishlistItems(User user) {
        return wishlistRepository.findByUser(user);
    }
    
    @Transactional
    public void removeFromWishlist(Long wishlistId, User user) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));
        
        if (!wishlist.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
        
        wishlistRepository.delete(wishlist);
    }
    
    public int getWishlistCount(User user) {
        return wishlistRepository.countByUserId(user.getId());
    }
    
    public boolean isInWishlist(User user, Long medicineId) {
        return wishlistRepository.existsByUserAndMedicineId(user, medicineId);
    }
}
