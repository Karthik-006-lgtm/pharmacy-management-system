package com.pharmacy.service;

import com.pharmacy.entity.MedicineRequest;
import com.pharmacy.entity.Notification;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.User;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.MedicineRequestRepository;
import com.pharmacy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicineRequestService {
    
    private final MedicineRequestRepository medicineRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    private static final Double DEFAULT_DELIVERY_RADIUS_KM = 10.0;
    
    public MedicineRequestService(MedicineRequestRepository medicineRequestRepository,
                                 UserRepository userRepository,
                                 NotificationService notificationService) {
        this.medicineRequestRepository = medicineRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public MedicineRequest createAndBroadcastRequest(User customer, String medicineName, 
                                                    Integer quantity, Order order) {
        String requestNumber = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        MedicineRequest request = MedicineRequest.builder()
                .requestNumber(requestNumber)
                .customer(customer)
                .medicineName(medicineName)
                .quantity(quantity)
                .order(order)
                .customerAddress(customer.getAddress())
                .customerCity(customer.getCity())
                .customerState(customer.getState())
                .customerPincode(customer.getPincode())
                .customerPhone(customer.getPhone())
                .customerLatitude(customer.getLatitude())
                .customerLongitude(customer.getLongitude())
                .status(MedicineRequest.RequestStatus.BROADCAST)
                .build();
        
        MedicineRequest savedRequest = medicineRequestRepository.save(request);
        
        broadcastToPharmacists(savedRequest);
        
        return savedRequest;
    }
    
    private void broadcastToPharmacists(MedicineRequest request) {
        List<User> onlinePharmacists = userRepository.findByIsOnlineTrue().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "PHARMACIST".equals(role.getName())))
                .collect(Collectors.toList());
        
        for (User pharmacist : onlinePharmacists) {
            if (isWithinDeliveryRadius(request, pharmacist)) {
                Double distance = calculateDistance(
                        request.getCustomerLatitude(), request.getCustomerLongitude(),
                        pharmacist.getLatitude(), pharmacist.getLongitude()
                );
                
                String message = String.format(
                        "New medicine request: %s (Qty: %d) | Customer: %s | Distance: %.2f km | Location: %s",
                        request.getMedicineName(),
                        request.getQuantity(),
                        request.getCustomer().getFullName(),
                        distance,
                        request.getCustomerCity()
                );
                
                notificationService.createNotification(
                        pharmacist,
                        Notification.NotificationType.MEDICINE_REQUEST,
                        "New Medicine Request",
                        message,
                        "MedicineRequest",
                        request.getId()
                );
            }
        }
    }
    
    @Transactional
    public MedicineRequest acceptRequest(Long requestId, User pharmacist) {
        MedicineRequest request = medicineRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine request not found"));
        
        if (request.getStatus() != MedicineRequest.RequestStatus.BROADCAST) {
            throw new RuntimeException("Request already accepted by another pharmacist");
        }
        
        request.setAcceptedPharmacist(pharmacist);
        request.setStatus(MedicineRequest.RequestStatus.ACCEPTED);
        request.setAcceptedAt(LocalDateTime.now());
        
        MedicineRequest savedRequest = medicineRequestRepository.save(request);
        
        notificationService.createNotification(
                request.getCustomer(),
                Notification.NotificationType.REQUEST_ACCEPTED,
                "Request Accepted",
                String.format("Your medicine request has been accepted by %s pharmacy. Order ID: %s",
                        pharmacist.getFullName(), request.getOrder().getOrderNumber()),
                "MedicineRequest",
                request.getId()
        );
        
        return savedRequest;
    }
    
    private boolean isWithinDeliveryRadius(MedicineRequest request, User pharmacist) {
        if (request.getCustomerLatitude() == null || request.getCustomerLongitude() == null ||
            pharmacist.getLatitude() == null || pharmacist.getLongitude() == null) {
            return true;
        }
        
        double distance = calculateDistance(
                request.getCustomerLatitude(), request.getCustomerLongitude(),
                pharmacist.getLatitude(), pharmacist.getLongitude()
        );
        
        return distance <= DEFAULT_DELIVERY_RADIUS_KM;
    }
    
    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 5.0;
        }
        
        final int EARTH_RADIUS = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    public List<MedicineRequest> getCustomerRequests(User customer) {
        return medicineRequestRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }
    
    public List<MedicineRequest> getPharmacistRequests(User pharmacist) {
        return medicineRequestRepository.findByAcceptedPharmacistOrderByAcceptedAtDesc(pharmacist);
    }
    
    public List<MedicineRequest> getBroadcastRequests() {
        return medicineRequestRepository.findByStatusOrderByBroadcastAtDesc(
                MedicineRequest.RequestStatus.BROADCAST);
    }
    
    public MedicineRequest findById(Long id) {
        return medicineRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine request not found"));
    }
    
    @Transactional
    public MedicineRequest updateRequestStatus(Long requestId, MedicineRequest.RequestStatus status) {
        MedicineRequest request = findById(requestId);
        request.setStatus(status);
        
        if (status == MedicineRequest.RequestStatus.DELIVERED) {
            request.setCompletedAt(LocalDateTime.now());
        }
        
        return medicineRequestRepository.save(request);
    }
}
