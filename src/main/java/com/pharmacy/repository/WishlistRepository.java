package com.pharmacy.repository;

import com.pharmacy.entity.User;
import com.pharmacy.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUser(User user);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.medicine.id = :medicineId")
    Optional<Wishlist> findByUserIdAndMedicineId(@Param("userId") Long userId, @Param("medicineId") Long medicineId);
    
    boolean existsByUserAndMedicineId(User user, Long medicineId);
    
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId")
    int countByUserId(@Param("userId") Long userId);
}
