package com.pharmacy.controller;

import com.pharmacy.entity.Prescription;
import com.pharmacy.entity.User;
import com.pharmacy.service.PrescriptionService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;

@Controller
@RequestMapping("/prescriptions")
public class PrescriptionController {
    
    private final PrescriptionService prescriptionService;
    private final SecurityUtil securityUtil;
    
    public PrescriptionController(PrescriptionService prescriptionService, SecurityUtil securityUtil) {
        this.prescriptionService = prescriptionService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadPrescription(@PathVariable Long id) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            Prescription prescription = prescriptionService.getById(id);
            
            // Check authorization - user must own the prescription or be admin/pharmacist
            boolean isAuthorized = prescription.getUser().getId().equals(currentUser.getId()) ||
                    currentUser.getRoles().stream().anyMatch(role -> 
                        role.getName().equals("ROLE_ADMIN") || 
                        role.getName().equals("ROLE_PHARMACIST"));
            
            if (!isAuthorized) {
                return ResponseEntity.status(403).build();
            }
            
            File file = new File(prescription.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + prescription.getFileName());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
