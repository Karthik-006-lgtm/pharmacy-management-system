package com.pharmacy.repository;

import com.pharmacy.entity.DeliveryTracking;
import com.pharmacy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {
    Optional<DeliveryTracking> findByOrder(Order order);
    Optional<DeliveryTracking> findByOrderId(Long orderId);
    List<DeliveryTracking> findByPharmacistId(Long pharmacistId);
    List<DeliveryTracking> findByPharmacistIdAndCurrentStatus(Long pharmacistId, DeliveryTracking.TrackingStatus status);
}
