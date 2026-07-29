package com.pharmacy.controller;

import com.pharmacy.dto.MedicineDto;
import com.pharmacy.entity.Medicine;
import com.pharmacy.service.CategoryService;
import com.pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/medicines")
public class AdminMedicineController {
    
    private final MedicineService medicineService;
    private final CategoryService categoryService;
    private final com.pharmacy.util.SecurityUtil securityUtil;
    
    public AdminMedicineController(MedicineService medicineService, CategoryService categoryService, com.pharmacy.util.SecurityUtil securityUtil) {
        this.medicineService = medicineService;
        this.categoryService = categoryService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping
    public String listMedicines(Model model) {
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "admin/medicines/list";
    }
    
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("medicine", new MedicineDto());
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "admin/medicines/form";
    }
    
    @PostMapping("/add")
    public String addMedicine(@Valid @ModelAttribute("medicine") MedicineDto dto,
                              BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            return "admin/medicines/form";
        }
        
        try {
            medicineService.createMedicine(dto, securityUtil.getCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Medicine added successfully");
            return "redirect:/admin/medicines";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/medicines/add";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Medicine medicine = medicineService.findById(id);
        
        MedicineDto dto = MedicineDto.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .brand(medicine.getBrand())
                .categoryId(medicine.getCategory().getId())
                .manufacturer(medicine.getManufacturer())
                .description(medicine.getDescription())
                .price(medicine.getPrice())
                .stock(medicine.getStock())
                .expiryDate(medicine.getExpiryDate())
                .manufactureDate(medicine.getManufactureDate())
                .prescriptionRequired(medicine.getPrescriptionRequired())
                .imageUrl(medicine.getImageUrl())
                .active(medicine.getActive())
                .build();
        
        model.addAttribute("medicine", dto);
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "admin/medicines/form";
    }
    
    @PostMapping("/edit/{id}")
    public String updateMedicine(@PathVariable Long id,
                                 @Valid @ModelAttribute("medicine") MedicineDto dto,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            return "admin/medicines/form";
        }
        
        try {
            medicineService.updateMedicine(id, dto, securityUtil.getCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Medicine updated successfully");
            return "redirect:/admin/medicines";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/medicines/edit/" + id;
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            medicineService.deleteMedicine(id, securityUtil.getCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Medicine deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/medicines";
    }
    
    @PostMapping("/upload-image")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> uploadImage(@org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("src/main/resources/static/uploads");
            
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            
            java.nio.file.Path filePath = uploadPath.resolve(fileName);
            java.nio.file.Files.write(filePath, file.getBytes());
            
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("imageUrl", "/uploads/" + fileName));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
