package com.pharmacy.repository;

import com.pharmacy.entity.DeliveryFeedback;
import com.pharmacy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryFeedbackRepository extends JpaRepository<DeliveryFeedback, Long> {
    Optional<DeliveryFeedback> findByOrder(Order order);
    Optional<DeliveryFeedback> findByOrderId(Long orderId);
    List<DeliveryFeedback> findByCustomerId(Long customerId);
    List<DeliveryFeedback> findByPharmacistId(Long pharmacistId);
    
    @Query("SELECT AVG(f.overallRating) FROM DeliveryFeedback f WHERE f.pharmacist.id = :pharmacistId")
    Double calculateAverageRatingForPharmacist(Long pharmacistId);
    
    @Query("SELECT COUNT(f) FROM DeliveryFeedback f WHERE f.pharmacist.id = :pharmacistId")
    Long countFeedbackForPharmacist(Long pharmacistId);
}
