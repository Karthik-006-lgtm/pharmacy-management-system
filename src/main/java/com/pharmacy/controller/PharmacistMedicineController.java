package com.pharmacy.controller;

import com.pharmacy.dto.MedicineDto;
import com.pharmacy.entity.Category;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.User;
import com.pharmacy.service.CategoryService;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pharmacist/medicines")
public class PharmacistMedicineController {
    
    private final MedicineService medicineService;
    private final CategoryService categoryService;
    private final UserService userService;
    
    public PharmacistMedicineController(MedicineService medicineService, CategoryService categoryService, UserService userService) {
        this.medicineService = medicineService;
        this.categoryService = categoryService;
        this.userService = userService;
    }
    
    @GetMapping
    public String listMedicines(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        List<Medicine> medicines = medicineService.getByPharmacist(pharmacist.getId());
        
        model.addAttribute("medicines", medicines);
        model.addAttribute("pharmacist", pharmacist);
        return "pharmacist/medicines/list";
    }
    
    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<Category> categories = categoryService.getAllActiveCategories();
        model.addAttribute("medicineDto", new MedicineDto());
        model.addAttribute("categories", categories);
        return "pharmacist/medicines/add";
    }
    
    @PostMapping("/add")
    public String addMedicine(@Valid @ModelAttribute MedicineDto medicineDto,
                              BindingResult result,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            List<Category> categories = categoryService.getAllActiveCategories();
            model.addAttribute("categories", categories);
            return "pharmacist/medicines/add";
        }
        
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        medicineService.createByPharmacist(medicineDto, pharmacist);
        
        redirectAttributes.addFlashAttribute("successMessage", "Medicine added successfully!");
        return "redirect:/pharmacist/medicines";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, 
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        Medicine medicine = medicineService.getById(id);
        
        // Verify this medicine belongs to this pharmacist
        if (medicine.getUploadedBy() == null || !medicine.getUploadedBy().getId().equals(pharmacist.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit medicines uploaded by you!");
            return "redirect:/pharmacist/medicines";
        }
        
        MedicineDto medicineDto = medicineService.convertToDto(medicine);
        List<Category> categories = categoryService.getAllActiveCategories();
        
        model.addAttribute("medicineDto", medicineDto);
        model.addAttribute("medicine", medicine);
        model.addAttribute("categories", categories);
        return "pharmacist/medicines/edit";
    }
    
    @PostMapping("/edit/{id}")
    public String updateMedicine(@PathVariable Long id,
                                 @Valid @ModelAttribute MedicineDto medicineDto,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        Medicine medicine = medicineService.getById(id);
        
        // Verify ownership
        if (medicine.getUploadedBy() == null || !medicine.getUploadedBy().getId().equals(pharmacist.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit medicines uploaded by you!");
            return "redirect:/pharmacist/medicines";
        }
        
        if (result.hasErrors()) {
            List<Category> categories = categoryService.getAllActiveCategories();
            model.addAttribute("medicine", medicine);
            model.addAttribute("categories", categories);
            return "pharmacist/medicines/edit";
        }
        
        medicineService.updateByPharmacist(id, medicineDto);
        redirectAttributes.addFlashAttribute("successMessage", "Medicine updated successfully!");
        return "redirect:/pharmacist/medicines";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        Medicine medicine = medicineService.getById(id);
        
        // Verify ownership
        if (medicine.getUploadedBy() == null || !medicine.getUploadedBy().getId().equals(pharmacist.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only delete medicines uploaded by you!");
            return "redirect:/pharmacist/medicines";
        }
        
        medicineService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Medicine deleted successfully!");
        return "redirect:/pharmacist/medicines";
    }
    
    @PostMapping("/update-stock/{id}")
    public String updateStock(@PathVariable Long id,
                              @RequestParam Integer stock,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        Medicine medicine = medicineService.getById(id);
        
        if (medicine.getUploadedBy() == null || !medicine.getUploadedBy().getId().equals(pharmacist.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized action!");
            return "redirect:/pharmacist/medicines";
        }
        
        medicineService.updateStock(id, stock);
        redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully!");
        return "redirect:/pharmacist/medicines";
    }
}
