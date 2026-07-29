package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Page<Medicine> findByActiveTrue(Pageable pageable);
    
    @Query("SELECT m FROM Medicine m WHERE m.active = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Medicine> searchMedicines(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT m FROM Medicine m WHERE m.active = true AND m.category.id = :categoryId")
    Page<Medicine> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT m FROM Medicine m WHERE m.active = true AND m.prescriptionRequired = :required")
    Page<Medicine> findByPrescriptionRequired(@Param("required") Boolean required, Pageable pageable);
    
    @Query("SELECT m FROM Medicine m WHERE m.stock <= 10")
    List<Medicine> findLowStockMedicines();
    
    @Query("SELECT m FROM Medicine m WHERE m.expiryDate <= :date")
    List<Medicine> findExpiringMedicines(@Param("date") LocalDate date);
    
    List<Medicine> findByUploadedById(Long pharmacistId);
    
    long countByUploadedById(Long pharmacistId);
    
    long countByActiveTrue();
}
