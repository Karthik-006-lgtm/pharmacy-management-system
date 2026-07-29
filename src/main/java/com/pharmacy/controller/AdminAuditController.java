package com.pharmacy.controller;

import com.pharmacy.service.AuditLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/audit")
public class AdminAuditController {
    
    private final AuditLogService auditLogService;
    
    public AdminAuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    
    @GetMapping
    public String viewAuditLogs(Model model) {
        model.addAttribute("logs", auditLogService.getRecentLogs(100));
        return "admin/audit/list";
    }
}
