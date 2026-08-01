package com.pharmacy.controller;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.User;
import com.pharmacy.service.CartService;
import com.pharmacy.service.CategoryService;
import com.pharmacy.service.MedicineService;
import com.pharmacy.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    
    private final MedicineService medicineService;
    private final CategoryService categoryService;
    private final SecurityUtil securityUtil;
    private final CartService cartService;
    
    public HomeController(MedicineService medicineService, CategoryService categoryService,
                          SecurityUtil securityUtil, CartService cartService) {
        this.medicineService = medicineService;
        this.categoryService = categoryService;
        this.securityUtil = securityUtil;
        this.cartService = cartService;
    }
    
    @GetMapping("/home")
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "createdAt") String sortBy,
                       @RequestParam(required = false) Boolean prescription,
                       Model model) {
        Page<Medicine> medicines;
        
        if (prescription != null) {
            medicines = medicineService.getMedicinesByPrescriptionRequired(prescription, page, 12);
        } else {
            medicines = medicineService.getAllActiveMedicines(page, 12, sortBy);
        }
        
        model.addAttribute("medicines", medicines);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", medicines.getTotalPages());
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("prescription", prescription);
        
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("cartCount", cartService.getCartItemCount(currentUser));
        }
        
        return "home";
    }
    
    @GetMapping("/medicines")
    public String medicines(@RequestParam(required = false) String search,
                            @RequestParam(required = false) Long category,
                            @RequestParam(required = false) Boolean prescription,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Page<Medicine> medicines;
        
        if (search != null && !search.isEmpty()) {
            medicines = medicineService.searchMedicines(search, page, 12);
        } else if (category != null) {
            medicines = medicineService.getMedicinesByCategory(category, page, 12);
        } else if (prescription != null) {
            medicines = medicineService.getMedicinesByPrescriptionRequired(prescription, page, 12);
        } else {
            medicines = medicineService.getAllActiveMedicines(page, 12, "createdAt");
        }
        
        model.addAttribute("medicines", medicines);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", medicines.getTotalPages());
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", category);
        
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("cartCount", cartService.getCartItemCount(currentUser));
        }
        
        return "medicines/list";
    }
    
    @GetMapping("/medicines/{id}")
    public String medicineDetails(@PathVariable Long id, Model model) {
        Medicine medicine = medicineService.findById(id);
        model.addAttribute("medicine", medicine);
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("cartCount", cartService.getCartItemCount(currentUser));
        }
        
        return "medicines/details";
    }
    
}
