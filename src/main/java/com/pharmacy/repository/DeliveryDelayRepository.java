package com.pharmacy.repository;

import com.pharmacy.entity.DeliveryDelay;
import com.pharmacy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryDelayRepository extends JpaRepository<DeliveryDelay, Long> {
    List<DeliveryDelay> findByOrderOrderByReportedAtDesc(Order order);
    List<DeliveryDelay> findByOrderIdOrderByReportedAtDesc(Long orderId);
    List<DeliveryDelay> findByPharmacistId(Long pharmacistId);
}
