package com.pharmacy.repository;

import com.pharmacy.entity.Cart;
import com.pharmacy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User user);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.medicine.id = :medicineId")
    Optional<Cart> findByUserIdAndMedicineId(@Param("userId") Long userId, @Param("medicineId") Long medicineId);
    
    void deleteByUser(User user);
    
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user.id = :userId")
    int countByUserId(@Param("userId") Long userId);
}
