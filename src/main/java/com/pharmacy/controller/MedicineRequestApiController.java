package com.pharmacy.controller;

import com.pharmacy.entity.MedicineRequest;
import com.pharmacy.entity.User;
import com.pharmacy.service.MedicineRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicine-requests")
public class MedicineRequestApiController {
    
    private final MedicineRequestService medicineRequestService;
    private final com.pharmacy.service.UserService userService;
    
    public MedicineRequestApiController(MedicineRequestService medicineRequestService,
                                       com.pharmacy.service.UserService userService) {
        this.medicineRequestService = medicineRequestService;
        this.userService = userService;
    }
    
    @GetMapping("/broadcast")
    public ResponseEntity<List<MedicineRequest>> getBroadcastRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<MedicineRequest> requests = medicineRequestService.getBroadcastRequests();
        return ResponseEntity.ok(requests);
    }
    
    @PostMapping("/{id}/accept")
    public ResponseEntity<Map<String, Object>> acceptRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User pharmacist = userService.findByEmail(userDetails.getUsername());
            MedicineRequest request = medicineRequestService.acceptRequest(id, pharmacist);
            
            response.put("success", true);
            response.put("message", "Request accepted successfully");
            response.put("requestNumber", request.getRequestNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/my-requests")
    public ResponseEntity<List<MedicineRequest>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<MedicineRequest> requests = medicineRequestService.getCustomerRequests(user);
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/pharmacist-requests")
    public ResponseEntity<List<MedicineRequest>> getPharmacistRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        User pharmacist = userService.findByEmail(userDetails.getUsername());
        List<MedicineRequest> requests = medicineRequestService.getPharmacistRequests(pharmacist);
        return ResponseEntity.ok(requests);
    }
}
