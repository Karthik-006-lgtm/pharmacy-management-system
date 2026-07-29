package com.pharmacy.service;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.Prescription;
import com.pharmacy.entity.User;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PrescriptionService {
    
    private final PrescriptionRepository prescriptionRepository;
    private final OrderService orderService;
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    public PrescriptionService(PrescriptionRepository prescriptionRepository, OrderService orderService) {
        this.prescriptionRepository = prescriptionRepository;
        this.orderService = orderService;
    }
    
    @Transactional
    public Prescription uploadPrescription(User user, Long orderId, MultipartFile file) throws IOException {
        Order order = orderService.findById(orderId);
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());
        
        Prescription prescription = Prescription.builder()
                .user(user)
                .order(order)
                .fileName(file.getOriginalFilename())
                .filePath(filePath.toString())
                .status(Prescription.PrescriptionStatus.PENDING)
                .build();
        
        return prescriptionRepository.save(prescription);
    }
    
    public Prescription findById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
    }
    
    public Prescription findByOrderId(Long orderId) {
        return prescriptionRepository.findByOrderId(orderId)
                .orElse(null);
    }
    
    public List<Prescription> getPendingPrescriptions() {
        return prescriptionRepository.findByStatus(Prescription.PrescriptionStatus.PENDING);
    }
    
    public List<Prescription> getUserPrescriptions(User user) {
        return prescriptionRepository.findByUserId(user.getId());
    }
    
    @Transactional
    public Prescription approvePrescription(Long prescriptionId, User admin) {
        Prescription prescription = findById(prescriptionId);
        prescription.setStatus(Prescription.PrescriptionStatus.APPROVED);
        prescription.setVerifiedAt(LocalDateTime.now());
        prescription.setVerifiedBy(admin);
        
        orderService.updateOrderStatus(prescription.getOrder().getId(), Order.OrderStatus.APPROVED);
        
        return prescriptionRepository.save(prescription);
    }
    
    @Transactional
    public Prescription rejectPrescription(Long prescriptionId, User admin, String remarks) {
        Prescription prescription = findById(prescriptionId);
        prescription.setStatus(Prescription.PrescriptionStatus.REJECTED);
        prescription.setVerifiedAt(LocalDateTime.now());
        prescription.setVerifiedBy(admin);
        prescription.setAdminRemarks(remarks);
        
        orderService.updateOrderStatus(prescription.getOrder().getId(), Order.OrderStatus.CANCELLED);
        
        return prescriptionRepository.save(prescription);
    }
}
