package com.pharmacy.controller;

import com.pharmacy.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    
    private final MedicineService medicineService;
    private final OrderService orderService;
    private final UserService userService;
    private final PrescriptionService prescriptionService;
    private final AuditLogService auditLogService;
    
    public AdminDashboardController(MedicineService medicineService, OrderService orderService,
                                    UserService userService, PrescriptionService prescriptionService,
                                    AuditLogService auditLogService) {
        this.medicineService = medicineService;
        this.orderService = orderService;
        this.userService = userService;
        this.prescriptionService = prescriptionService;
        this.auditLogService = auditLogService;
    }
    
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalMedicines", medicineService.getTotalMedicines());
        model.addAttribute("totalOrders", orderService.getTotalOrders());
        model.addAttribute("totalCustomers", userService.getTotalCustomers());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines());
        model.addAttribute("expiringMedicines", medicineService.getExpiringMedicines());
        model.addAttribute("expiring30Days", medicineService.getExpiringWithinDays(30));
        model.addAttribute("expiring15Days", medicineService.getExpiringWithinDays(15));
        model.addAttribute("expiring7Days", medicineService.getExpiringWithinDays(7));
        model.addAttribute("expiredMedicines", medicineService.getExpiredMedicines());
        model.addAttribute("recentOrders", orderService.getRecentOrders(5));
        model.addAttribute("pendingPrescriptions", prescriptionService.getPendingPrescriptions());
        model.addAttribute("recentAuditLogs", auditLogService.getRecentLogs(10));
        model.addAttribute("todayOrders", orderService.getTodayOrders());
        model.addAttribute("pendingOrders", orderService.getPendingOrders());
        model.addAttribute("deliveredOrders", orderService.getDeliveredOrders());
        
        return "admin/dashboard";
    }
}
