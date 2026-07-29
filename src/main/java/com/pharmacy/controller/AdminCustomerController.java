package com.pharmacy.controller;

import com.pharmacy.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {
    
    private final UserService userService;
    
    public AdminCustomerController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", userService.getAllCustomers());
        return "admin/customers/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        model.addAttribute("customer", userService.findById(id));
        return "admin/customers/view";
    }
}
