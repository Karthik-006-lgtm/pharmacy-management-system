package com.pharmacy.repository;

import com.pharmacy.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByOrderId(Long orderId);
    
    @Query("SELECT p FROM Prescription p WHERE p.status = :status ORDER BY p.uploadedAt DESC")
    List<Prescription> findByStatus(@Param("status") Prescription.PrescriptionStatus status);
    
    @Query("SELECT p FROM Prescription p WHERE p.user.id = :userId ORDER BY p.uploadedAt DESC")
    List<Prescription> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Prescription p WHERE p.pharmacist.id = :pharmacistId ORDER BY p.uploadedAt DESC")
    List<Prescription> findByPharmacistId(@Param("pharmacistId") Long pharmacistId);
}
