package com.pharmacy.config;

import com.pharmacy.entity.Category;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Role;
import com.pharmacy.entity.User;
import com.pharmacy.repository.CategoryRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.RoleRepository;
import com.pharmacy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository,
                                      CategoryRepository categoryRepository, MedicineRepository medicineRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                roleRepository.save(adminRole);
                
                Role customerRole = new Role();
                customerRole.setName("ROLE_CUSTOMER");
                roleRepository.save(customerRole);
                
                Role pharmacistRole = new Role();
                pharmacistRole.setName("ROLE_PHARMACIST");
                roleRepository.save(pharmacistRole);
                
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                
                User admin = User.builder()
                        .fullName("Admin User")
                        .email("admin@pharmacy.com")
                        .password(passwordEncoder.encode("admin123"))
                        .phone("9876543210")
                        .address("123 Admin Street")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .pincode("400001")
                        .roles(adminRoles)
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                
                Set<Role> customerRoles = new HashSet<>();
                customerRoles.add(customerRole);
                
                User customer = User.builder()
                        .fullName("John Doe")
                        .email("john@example.com")
                        .password(passwordEncoder.encode("john123"))
                        .phone("9123456789")
                        .address("456 Customer Lane")
                        .city("Delhi")
                        .state("Delhi")
                        .pincode("110001")
                        .roles(customerRoles)
                        .enabled(true)
                        .build();
                userRepository.save(customer);
            }
            
            if (categoryRepository.count() == 0) {
                Category[] categories = {
                        Category.builder().name("Pain Relief").description("Medicines for pain management").active(true).build(),
                        Category.builder().name("Cold & Flu").description("Cold and flu remedies").active(true).build(),
                        Category.builder().name("Antibiotics").description("Antibiotic medications").active(true).build(),
                        Category.builder().name("Vitamins").description("Vitamin supplements").active(true).build(),
                        Category.builder().name("Diabetes Care").description("Diabetes management medicines").active(true).build(),
                        Category.builder().name("Heart Health").description("Cardiovascular medicines").active(true).build(),
                        Category.builder().name("Skin Care").description("Dermatological products").active(true).build(),
                        Category.builder().name("Digestive Health").description("Digestive system medicines").active(true).build()
                };
                
                for (Category category : categories) {
                    categoryRepository.save(category);
                }
            }
            
            if (medicineRepository.count() == 0) {
                Category painRelief = categoryRepository.findByName("Pain Relief").orElseThrow();
                Category coldFlu = categoryRepository.findByName("Cold & Flu").orElseThrow();
                Category antibiotics = categoryRepository.findByName("Antibiotics").orElseThrow();
                Category vitamins = categoryRepository.findByName("Vitamins").orElseThrow();
                Category diabetesCare = categoryRepository.findByName("Diabetes Care").orElseThrow();
                Category heartHealth = categoryRepository.findByName("Heart Health").orElseThrow();
                Category skinCare = categoryRepository.findByName("Skin Care").orElseThrow();
                Category digestiveHealth = categoryRepository.findByName("Digestive Health").orElseThrow();
                
                Medicine[] medicines = {
                        // Pain Relief Medicines
                        Medicine.builder()
                                .name("Paracetamol 500mg")
                                .brand("Crocin")
                                .category(painRelief)
                                .manufacturer("GSK")
                                .description("Effective pain relief and fever reducer")
                                .price(new BigDecimal("50.00"))
                                .stock(200)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Ibuprofen 400mg")
                                .brand("Brufen")
                                .category(painRelief)
                                .manufacturer("Abbott")
                                .description("Anti-inflammatory and pain relief")
                                .price(new BigDecimal("75.00"))
                                .stock(150)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Aspirin 75mg")
                                .brand("Disprin")
                                .category(painRelief)
                                .manufacturer("Reckitt Benckiser")
                                .description("Pain relief and blood thinner")
                                .price(new BigDecimal("40.00"))
                                .stock(180)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Diclofenac Sodium 50mg")
                                .brand("Voveran")
                                .category(painRelief)
                                .manufacturer("Novartis")
                                .description("Strong anti-inflammatory for severe pain")
                                .price(new BigDecimal("85.00"))
                                .stock(120)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(4))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Tramadol 50mg")
                                .brand("Ultracet")
                                .category(painRelief)
                                .manufacturer("Johnson & Johnson")
                                .description("Opioid pain medication for moderate to severe pain")
                                .price(new BigDecimal("120.00"))
                                .stock(80)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(5))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        // Cold & Flu Medicines
                        Medicine.builder()
                                .name("Cetirizine 10mg")
                                .brand("Cetzine")
                                .category(coldFlu)
                                .manufacturer("Dr. Reddy's")
                                .description("Antihistamine for allergy relief")
                                .price(new BigDecimal("60.00"))
                                .stock(100)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(1))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Chlorpheniramine Maleate 4mg")
                                .brand("Cheston Cold")
                                .category(coldFlu)
                                .manufacturer("Cipla")
                                .description("Relief from cold and flu symptoms")
                                .price(new BigDecimal("45.00"))
                                .stock(150)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Dextromethorphan Syrup")
                                .brand("Benadryl Cough")
                                .category(coldFlu)
                                .manufacturer("Johnson & Johnson")
                                .description("Cough suppressant syrup")
                                .price(new BigDecimal("85.00"))
                                .stock(90)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Loratadine 10mg")
                                .brand("Lorfast")
                                .category(coldFlu)
                                .manufacturer("Sun Pharma")
                                .description("Non-drowsy allergy relief")
                                .price(new BigDecimal("70.00"))
                                .stock(110)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(4))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        // Antibiotics
                        Medicine.builder()
                                .name("Amoxicillin 500mg")
                                .brand("Novamox")
                                .category(antibiotics)
                                .manufacturer("Cipla")
                                .description("Broad-spectrum antibiotic")
                                .price(new BigDecimal("120.00"))
                                .stock(80)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(4))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Azithromycin 250mg")
                                .brand("Azithral")
                                .category(antibiotics)
                                .manufacturer("Alembic")
                                .description("Antibiotic for bacterial infections")
                                .price(new BigDecimal("150.00"))
                                .stock(60)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Ciprofloxacin 500mg")
                                .brand("Ciplox")
                                .category(antibiotics)
                                .manufacturer("Cipla")
                                .description("Antibiotic for urinary and respiratory infections")
                                .price(new BigDecimal("110.00"))
                                .stock(70)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(5))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Doxycycline 100mg")
                                .brand("Doxycip")
                                .category(antibiotics)
                                .manufacturer("Cipla")
                                .description("Broad-spectrum antibiotic for various infections")
                                .price(new BigDecimal("130.00"))
                                .stock(55)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(6))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Cefixime 200mg")
                                .brand("Mahacef")
                                .category(antibiotics)
                                .manufacturer("Mankind")
                                .description("Third-generation cephalosporin antibiotic")
                                .price(new BigDecimal("140.00"))
                                .stock(65)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        // Vitamins & Supplements
                        Medicine.builder()
                                .name("Vitamin C 500mg")
                                .brand("HealthVit")
                                .category(vitamins)
                                .manufacturer("HealthKart")
                                .description("Immunity booster")
                                .price(new BigDecimal("200.00"))
                                .stock(120)
                                .expiryDate(LocalDate.now().plusYears(3))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Multivitamin Tablets")
                                .brand("Revital")
                                .category(vitamins)
                                .manufacturer("Ranbaxy")
                                .description("Complete multivitamin supplement")
                                .price(new BigDecimal("350.00"))
                                .stock(90)
                                .expiryDate(LocalDate.now().plusYears(3))
                                .manufactureDate(LocalDate.now().minusMonths(1))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Vitamin D3 60000 IU")
                                .brand("Uprise D3")
                                .category(vitamins)
                                .manufacturer("Alkem")
                                .description("Bone health and calcium absorption")
                                .price(new BigDecimal("180.00"))
                                .stock(100)
                                .expiryDate(LocalDate.now().plusYears(3))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Calcium + Vitamin D3")
                                .brand("Shelcal")
                                .category(vitamins)
                                .manufacturer("Elder Pharma")
                                .description("Calcium supplement for bone health")
                                .price(new BigDecimal("220.00"))
                                .stock(85)
                                .expiryDate(LocalDate.now().plusYears(3))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Omega 3 Fish Oil")
                                .brand("HealthKart")
                                .category(vitamins)
                                .manufacturer("HealthKart")
                                .description("Heart and brain health supplement")
                                .price(new BigDecimal("450.00"))
                                .stock(70)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(4))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Iron + Folic Acid")
                                .brand("Autrin")
                                .category(vitamins)
                                .manufacturer("Merck")
                                .description("Treats iron deficiency anemia")
                                .price(new BigDecimal("120.00"))
                                .stock(95)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(5))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        // Diabetes Care
                        Medicine.builder()
                                .name("Metformin 500mg")
                                .brand("Glycomet")
                                .category(diabetesCare)
                                .manufacturer("USV")
                                .description("First-line medication for type 2 diabetes")
                                .price(new BigDecimal("90.00"))
                                .stock(150)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Glimepiride 2mg")
                                .brand("Amaryl")
                                .category(diabetesCare)
                                .manufacturer("Sanofi")
                                .description("Helps control blood sugar levels")
                                .price(new BigDecimal("110.00"))
                                .stock(130)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Insulin Glargine Injection")
                                .brand("Lantus")
                                .category(diabetesCare)
                                .manufacturer("Sanofi")
                                .description("Long-acting insulin for diabetes")
                                .price(new BigDecimal("650.00"))
                                .stock(40)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(4))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Sitagliptin 100mg")
                                .brand("Januvia")
                                .category(diabetesCare)
                                .manufacturer("MSD")
                                .description("DPP-4 inhibitor for type 2 diabetes")
                                .price(new BigDecimal("280.00"))
                                .stock(75)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        // Heart Health
                        Medicine.builder()
                                .name("Atorvastatin 10mg")
                                .brand("Lipitor")
                                .category(heartHealth)
                                .manufacturer("Pfizer")
                                .description("Cholesterol-lowering medication")
                                .price(new BigDecimal("140.00"))
                                .stock(160)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Amlodipine 5mg")
                                .brand("Amlong")
                                .category(heartHealth)
                                .manufacturer("Micro Labs")
                                .description("Blood pressure control medication")
                                .price(new BigDecimal("70.00"))
                                .stock(180)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Ramipril 2.5mg")
                                .brand("Cardace")
                                .category(heartHealth)
                                .manufacturer("Sanofi")
                                .description("ACE inhibitor for hypertension")
                                .price(new BigDecimal("95.00"))
                                .stock(140)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(4))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Clopidogrel 75mg")
                                .brand("Plavix")
                                .category(heartHealth)
                                .manufacturer("Sanofi")
                                .description("Antiplatelet medication to prevent blood clots")
                                .price(new BigDecimal("160.00"))
                                .stock(120)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(5))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Losartan 50mg")
                                .brand("Cozaar")
                                .category(heartHealth)
                                .manufacturer("MSD")
                                .description("Angiotensin receptor blocker for hypertension")
                                .price(new BigDecimal("105.00"))
                                .stock(135)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        // Skin Care
                        Medicine.builder()
                                .name("Clotrimazole Cream")
                                .brand("Candid")
                                .category(skinCare)
                                .manufacturer("Glenmark")
                                .description("Antifungal cream for skin infections")
                                .price(new BigDecimal("80.00"))
                                .stock(100)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Betamethasone Cream")
                                .brand("Betnovate")
                                .category(skinCare)
                                .manufacturer("GSK")
                                .description("Corticosteroid for skin inflammation")
                                .price(new BigDecimal("95.00"))
                                .stock(85)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufactureDate(LocalDate.now().minusMonths(4))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Adapalene Gel 0.1%")
                                .brand("Differin")
                                .category(skinCare)
                                .manufacturer("Galderma")
                                .description("Acne treatment gel")
                                .price(new BigDecimal("450.00"))
                                .stock(60)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Mometasone Cream")
                                .brand("Momate")
                                .category(skinCare)
                                .manufacturer("Glenmark")
                                .description("Treats eczema, psoriasis, and allergies")
                                .price(new BigDecimal("120.00"))
                                .stock(75)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(5))
                                .prescriptionRequired(true)
                                .active(true)
                                .build(),
                        
                        // Digestive Health
                        Medicine.builder()
                                .name("Omeprazole 20mg")
                                .brand("Omez")
                                .category(digestiveHealth)
                                .manufacturer("Dr. Reddy's")
                                .description("Proton pump inhibitor for acid reflux")
                                .price(new BigDecimal("90.00"))
                                .stock(170)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Ranitidine 150mg")
                                .brand("Aciloc")
                                .category(digestiveHealth)
                                .manufacturer("Cadila")
                                .description("H2 blocker for stomach acid")
                                .price(new BigDecimal("65.00"))
                                .stock(140)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Domperidone 10mg")
                                .brand("Domstal")
                                .category(digestiveHealth)
                                .manufacturer("Torrent")
                                .description("Relieves nausea and vomiting")
                                .price(new BigDecimal("55.00"))
                                .stock(125)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(4))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Loperamide 2mg")
                                .brand("Eldoper")
                                .category(digestiveHealth)
                                .manufacturer("Elder")
                                .description("Anti-diarrheal medication")
                                .price(new BigDecimal("48.00"))
                                .stock(110)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(2))
                                .prescriptionRequired(false)
                                .active(true)
                                .build(),
                        
                        Medicine.builder()
                                .name("Pancreatin Digestive Enzyme")
                                .brand("Creon")
                                .category(digestiveHealth)
                                .manufacturer("Abbott")
                                .description("Digestive enzyme supplement")
                                .price(new BigDecimal("250.00"))
                                .stock(65)
                                .expiryDate(LocalDate.now().plusYears(2))
                                .manufactureDate(LocalDate.now().minusMonths(3))
                                .prescriptionRequired(false)
                                .active(true)
                                .build()
                };
                
                for (Medicine medicine : medicines) {
                    medicineRepository.save(medicine);
                }
            }
        };
    }
}
