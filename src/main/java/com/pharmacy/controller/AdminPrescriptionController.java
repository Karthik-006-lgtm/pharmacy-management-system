package com.pharmacy.controller;

import com.pharmacy.entity.Prescription;
import com.pharmacy.entity.User;
import com.pharmacy.service.PrescriptionService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/prescriptions")
public class AdminPrescriptionController {
    
    private final PrescriptionService prescriptionService;
    private final SecurityUtil securityUtil;
    
    public AdminPrescriptionController(PrescriptionService prescriptionService, SecurityUtil securityUtil) {
        this.prescriptionService = prescriptionService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping
    public String listPrescriptions(Model model) {
        List<Prescription> pendingPrescriptions = prescriptionService.getPendingPrescriptions();
        model.addAttribute("prescriptions", pendingPrescriptions);
        return "admin/prescriptions/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewPrescription(@PathVariable Long id, Model model) {
        Prescription prescription = prescriptionService.findById(id);
        model.addAttribute("prescription", prescription);
        return "admin/prescriptions/view";
    }
    
    @PostMapping("/approve/{id}")
    public String approvePrescription(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User admin = securityUtil.getCurrentUser();
            prescriptionService.approvePrescription(id, admin);
            redirectAttributes.addFlashAttribute("success", "Prescription approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/prescriptions";
    }
    
    @PostMapping("/reject/{id}")
    public String rejectPrescription(@PathVariable Long id,
                                     @RequestParam String remarks,
                                     RedirectAttributes redirectAttributes) {
        try {
            User admin = securityUtil.getCurrentUser();
            prescriptionService.rejectPrescription(id, admin, remarks);
            redirectAttributes.addFlashAttribute("success", "Prescription rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/prescriptions";
    }
}
