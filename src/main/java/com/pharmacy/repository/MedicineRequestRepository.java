package com.pharmacy.repository;

import com.pharmacy.entity.MedicineRequest;
import com.pharmacy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRequestRepository extends JpaRepository<MedicineRequest, Long> {
    Optional<MedicineRequest> findByRequestNumber(String requestNumber);
    List<MedicineRequest> findByCustomerOrderByCreatedAtDesc(User customer);
    List<MedicineRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<MedicineRequest> findByAcceptedPharmacistOrderByAcceptedAtDesc(User pharmacist);
    List<MedicineRequest> findByAcceptedPharmacistIdOrderByAcceptedAtDesc(Long pharmacistId);
    List<MedicineRequest> findByStatus(MedicineRequest.RequestStatus status);
    List<MedicineRequest> findByStatusOrderByBroadcastAtDesc(MedicineRequest.RequestStatus status);
}
