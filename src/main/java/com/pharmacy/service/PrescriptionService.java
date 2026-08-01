package com.pharmacy.service;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.Prescription;
import com.pharmacy.entity.User;
import com.pharmacy.exception.FileUploadException;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PrescriptionService {
    
    private final PrescriptionRepository prescriptionRepository;
    private final String uploadDir = "uploads/prescriptions/";
    
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
        
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    @Transactional
    public Prescription uploadPrescription(User user, Order order, MultipartFile file, User pharmacist) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Please select a file to upload");
        }
        
        long maxFileSize = 10 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new FileUploadException("File size exceeds maximum limit of 10MB. Your file: " + 
                    (file.getSize() / (1024 * 1024)) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new FileUploadException("Invalid file name");
        }
        
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!fileExtension.matches("\\.(pdf|jpg|jpeg|png)$")) {
            throw new FileUploadException("Invalid file type. Only PDF, JPG, JPEG, and PNG files are allowed");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;
        
        Path filePath = Paths.get(uploadDir + filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        Prescription prescription = Prescription.builder()
                .user(user)
                .order(order)
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .status(Prescription.PrescriptionStatus.PENDING)
                .pharmacist(pharmacist)
                .build();
        
        return prescriptionRepository.save(prescription);
    }
    
    public Prescription getByOrderId(Long orderId) {
        return prescriptionRepository.findByOrderId(orderId)
                .orElse(null);
    }
    
    public Prescription getById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
    }
    
    public List<Prescription> getByUserId(Long userId) {
        return prescriptionRepository.findByUserId(userId);
    }
    
    public List<Prescription> getByPharmacistId(Long pharmacistId) {
        return prescriptionRepository.findByPharmacistId(pharmacistId);
    }
    
    public List<Prescription> getPendingPrescriptions() {
        return prescriptionRepository.findByStatus(Prescription.PrescriptionStatus.PENDING);
    }
    
    public Prescription findById(Long id) {
        return getById(id);
    }
    
    public Prescription findByOrderId(Long orderId) {
        return getByOrderId(orderId);
    }
    
    @Transactional
    public Prescription approvePrescription(Long id, User approver) {
        return approvePrescription(id, approver, "Approved");
    }
    
    @Transactional
    public Prescription approvePrescription(Long id, User approver, String remarks) {
        Prescription prescription = getById(id);
        prescription.setStatus(Prescription.PrescriptionStatus.APPROVED);
        prescription.setVerifiedBy(approver);
        prescription.setAdminRemarks(remarks);
        return prescriptionRepository.save(prescription);
    }
    
    @Transactional
    public Prescription rejectPrescription(Long id, User approver, String remarks) {
        Prescription prescription = getById(id);
        prescription.setStatus(Prescription.PrescriptionStatus.REJECTED);
        prescription.setVerifiedBy(approver);
        prescription.setAdminRemarks(remarks);
        return prescriptionRepository.save(prescription);
    }
}
