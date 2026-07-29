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
                
                Medicine[] medicines = {
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
                                .build()
                };
                
                for (Medicine medicine : medicines) {
                    medicineRepository.save(medicine);
                }
            }
        };
    }
}
