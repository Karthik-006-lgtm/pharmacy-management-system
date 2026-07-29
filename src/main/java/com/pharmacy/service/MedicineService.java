package com.pharmacy.service;

import com.pharmacy.dto.MedicineDto;
import com.pharmacy.entity.Category;
import com.pharmacy.entity.Medicine;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MedicineService {
    
    private final MedicineRepository medicineRepository;
    private final CategoryService categoryService;
    private final AuditLogService auditLogService;
    
    public MedicineService(MedicineRepository medicineRepository, CategoryService categoryService, AuditLogService auditLogService) {
        this.medicineRepository = medicineRepository;
        this.categoryService = categoryService;
        this.auditLogService = auditLogService;
    }
    
    public Page<Medicine> getAllActiveMedicines(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return medicineRepository.findByActiveTrue(pageable);
    }
    
    public Page<Medicine> searchMedicines(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return medicineRepository.searchMedicines(keyword, pageable);
    }
    
    public Page<Medicine> getMedicinesByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return medicineRepository.findByCategoryId(categoryId, pageable);
    }
    
    public Page<Medicine> getMedicinesByPrescriptionRequired(Boolean required, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return medicineRepository.findByPrescriptionRequired(required, pageable);
    }
    
    public Medicine findById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));
    }
    
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }
    
    @Transactional
    public Medicine createMedicine(MedicineDto dto, com.pharmacy.entity.User user) {
        Category category = categoryService.findById(dto.getCategoryId());
        
        Medicine medicine = Medicine.builder()
                .name(dto.getName())
                .brand(dto.getBrand())
                .category(category)
                .manufacturer(dto.getManufacturer())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .expiryDate(dto.getExpiryDate())
                .manufactureDate(dto.getManufactureDate())
                .prescriptionRequired(dto.getPrescriptionRequired() != null ? dto.getPrescriptionRequired() : false)
                .imageUrl(dto.getImageUrl())
                .active(true)
                .build();
        
        Medicine saved = medicineRepository.save(medicine);
        auditLogService.log("MEDICINE_ADDED", "Medicine", saved.getId(), "Added: " + saved.getName(), user);
        return saved;
    }
    
    @Transactional
    public Medicine updateMedicine(Long id, MedicineDto dto, com.pharmacy.entity.User user) {
        Medicine medicine = findById(id);
        Category category = categoryService.findById(dto.getCategoryId());
        
        medicine.setName(dto.getName());
        medicine.setBrand(dto.getBrand());
        medicine.setCategory(category);
        medicine.setManufacturer(dto.getManufacturer());
        medicine.setDescription(dto.getDescription());
        medicine.setPrice(dto.getPrice());
        medicine.setStock(dto.getStock());
        medicine.setExpiryDate(dto.getExpiryDate());
        medicine.setManufactureDate(dto.getManufactureDate());
        medicine.setPrescriptionRequired(dto.getPrescriptionRequired() != null ? dto.getPrescriptionRequired() : false);
        if (dto.getImageUrl() != null) {
            medicine.setImageUrl(dto.getImageUrl());
        }
        medicine.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        Medicine updated = medicineRepository.save(medicine);
        auditLogService.log("MEDICINE_UPDATED", "Medicine", updated.getId(), "Updated: " + updated.getName(), user);
        return updated;
    }
    
    @Transactional
    public void deleteMedicine(Long id, com.pharmacy.entity.User user) {
        Medicine medicine = findById(id);
        medicine.setActive(false);
        medicineRepository.save(medicine);
        auditLogService.log("MEDICINE_DELETED", "Medicine", id, "Deleted: " + medicine.getName(), user);
    }
    
    @Transactional
    public void updateStock(Long medicineId, int quantity) {
        Medicine medicine = findById(medicineId);
        medicine.setStock(medicine.getStock() - quantity);
        medicineRepository.save(medicine);
    }
    
    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines();
    }
    
    public List<Medicine> getExpiringMedicines() {
        LocalDate threeMonthsFromNow = LocalDate.now().plusMonths(3);
        return medicineRepository.findExpiringMedicines(threeMonthsFromNow);
    }
    
    public List<Medicine> getExpiringWithinDays(int days) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        return medicineRepository.findExpiringMedicines(targetDate);
    }
    
    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findExpiringMedicines(LocalDate.now());
    }
    
    public long getTotalMedicines() {
        return medicineRepository.countByActiveTrue();
    }
    
    public List<Medicine> getByPharmacist(Long pharmacistId) {
        return medicineRepository.findByUploadedById(pharmacistId);
    }
    
    public long countByPharmacist(Long pharmacistId) {
        return medicineRepository.countByUploadedById(pharmacistId);
    }
    
    @Transactional
    public Medicine createByPharmacist(MedicineDto dto, com.pharmacy.entity.User pharmacist) {
        Category category = categoryService.findById(dto.getCategoryId());
        
        Medicine medicine = Medicine.builder()
                .name(dto.getName())
                .brand(dto.getBrand())
                .category(category)
                .manufacturer(dto.getManufacturer())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .taxPercentage(dto.getTaxPercentage())
                .stock(dto.getStock())
                .expiryDate(dto.getExpiryDate())
                .manufactureDate(dto.getManufactureDate())
                .batchNumber(dto.getBatchNumber())
                .prescriptionRequired(dto.getPrescriptionRequired() != null ? dto.getPrescriptionRequired() : false)
                .imageUrl(dto.getImageUrl())
                .uploadedBy(pharmacist)
                .active(true)
                .build();
        
        Medicine saved = medicineRepository.save(medicine);
        auditLogService.log("MEDICINE_ADDED", "Medicine", saved.getId(), 
            "Pharmacist added medicine: " + saved.getName(), pharmacist);
        return saved;
    }
    
    @Transactional
    public Medicine updateByPharmacist(Long id, MedicineDto dto) {
        Medicine medicine = findById(id);
        Category category = categoryService.findById(dto.getCategoryId());
        
        medicine.setName(dto.getName());
        medicine.setBrand(dto.getBrand());
        medicine.setCategory(category);
        medicine.setManufacturer(dto.getManufacturer());
        medicine.setDescription(dto.getDescription());
        medicine.setPrice(dto.getPrice());
        medicine.setTaxPercentage(dto.getTaxPercentage());
        medicine.setStock(dto.getStock());
        medicine.setExpiryDate(dto.getExpiryDate());
        medicine.setManufactureDate(dto.getManufactureDate());
        medicine.setBatchNumber(dto.getBatchNumber());
        medicine.setPrescriptionRequired(dto.getPrescriptionRequired() != null ? dto.getPrescriptionRequired() : false);
        if (dto.getImageUrl() != null) {
            medicine.setImageUrl(dto.getImageUrl());
        }
        medicine.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        return medicineRepository.save(medicine);
    }
    
    public Medicine getById(Long id) {
        return findById(id);
    }
    
    @Transactional
    public void deleteById(Long id) {
        Medicine medicine = findById(id);
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }
    
    public MedicineDto convertToDto(Medicine medicine) {
        MedicineDto dto = new MedicineDto();
        dto.setId(medicine.getId());
        dto.setName(medicine.getName());
        dto.setBrand(medicine.getBrand());
        dto.setCategoryId(medicine.getCategory().getId());
        dto.setManufacturer(medicine.getManufacturer());
        dto.setDescription(medicine.getDescription());
        dto.setPrice(medicine.getPrice());
        dto.setTaxPercentage(medicine.getTaxPercentage());
        dto.setStock(medicine.getStock());
        dto.setExpiryDate(medicine.getExpiryDate());
        dto.setManufactureDate(medicine.getManufactureDate());
        dto.setBatchNumber(medicine.getBatchNumber());
        dto.setPrescriptionRequired(medicine.getPrescriptionRequired());
        dto.setImageUrl(medicine.getImageUrl());
        dto.setActive(medicine.getActive());
        return dto;
    }
}
