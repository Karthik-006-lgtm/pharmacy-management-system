package com.pharmacy.scheduled;

import com.pharmacy.entity.Medicine;
import com.pharmacy.repository.MedicineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class MedicineCleanupTask {
    
    private static final Logger logger = LoggerFactory.getLogger(MedicineCleanupTask.class);
    
    private final MedicineRepository medicineRepository;
    
    public MedicineCleanupTask(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }
    
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void deactivateExpiredMedicines() {
        logger.info("Starting expired medicines cleanup task...");
        
        LocalDate today = LocalDate.now();
        List<Medicine> allMedicines = medicineRepository.findAll();
        int deactivatedCount = 0;
        
        for (Medicine medicine : allMedicines) {
            if (medicine.getActive() && medicine.getExpiryDate().isBefore(today)) {
                medicine.setActive(false);
                medicineRepository.save(medicine);
                deactivatedCount++;
                logger.info("Deactivated expired medicine: {} (Expiry: {})", 
                        medicine.getName(), medicine.getExpiryDate());
            }
        }
        
        logger.info("Expired medicines cleanup completed. Deactivated {} medicines.", deactivatedCount);
    }
    
    @Scheduled(cron = "0 0 8 * * ?")
    public void logExpiringMedicines() {
        logger.info("Checking for medicines expiring soon...");
        
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<Medicine> expiringMedicines = medicineRepository.findExpiringMedicines(thirtyDaysFromNow);
        
        if (!expiringMedicines.isEmpty()) {
            logger.warn("Warning: {} medicines expiring within 30 days", expiringMedicines.size());
            expiringMedicines.forEach(medicine -> 
                logger.warn("Medicine '{}' expiring on {}", medicine.getName(), medicine.getExpiryDate())
            );
        } else {
            logger.info("No medicines expiring within 30 days");
        }
    }
}
