package com.pharmacy.controller;

import com.pharmacy.dto.UserRegistrationDto;
import com.pharmacy.entity.User;
import com.pharmacy.service.UserService;
import com.pharmacy.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private final UserService userService;
    private final SecurityUtil securityUtil;
    
    public ProfileController(UserService userService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.securityUtil = securityUtil;
    }
    
    @GetMapping
    public String viewProfile(Model model) {
        User currentUser = securityUtil.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "profile/view";
    }
    
    @GetMapping("/edit")
    public String editProfile(Model model) {
        User currentUser = securityUtil.getCurrentUser();
        
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName(currentUser.getFullName());
        dto.setEmail(currentUser.getEmail());
        dto.setPhone(currentUser.getPhone());
        dto.setAddress(currentUser.getAddress());
        dto.setCity(currentUser.getCity());
        dto.setState(currentUser.getState());
        dto.setPincode(currentUser.getPincode());
        
        model.addAttribute("user", dto);
        return "profile/edit";
    }
    
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("user") UserRegistrationDto dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profile/edit";
        }
        
        try {
            User currentUser = securityUtil.getCurrentUser();
            userService.updateUser(currentUser.getId(), dto);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}
