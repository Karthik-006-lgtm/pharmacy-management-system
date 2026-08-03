package com.pharmacy.service;

import com.pharmacy.dto.UserRegistrationDto;
import com.pharmacy.entity.Role;
import com.pharmacy.entity.User;
import com.pharmacy.exception.DuplicateResourceException;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.RoleRepository;
import com.pharmacy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    public UserService(UserRepository userRepository, RoleRepository roleRepository, 
                      PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        
        String roleName = "CUSTOMER".equals(registrationDto.getAccountType()) 
                ? "ROLE_CUSTOMER" : "ROLE_PHARMACIST";
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(roleName + " role not found"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        
        User user = User.builder()
                .fullName(registrationDto.getFullName())
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .phone(registrationDto.getPhone())
                .address(registrationDto.getAddress())
                .city(registrationDto.getCity())
                .state(registrationDto.getState())
                .pincode(registrationDto.getPincode())
                .roles(roles)
                .enabled(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Send registration email
        sendRegistrationEmail(savedUser, registrationDto.getAccountType());
        
        return savedUser;
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    @Transactional
    public User updateUser(Long id, UserRegistrationDto dto) {
        User user = findById(id);
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setCity(dto.getCity());
        user.setState(dto.getState());
        user.setPincode(dto.getPincode());
        return userRepository.save(user);
    }
    
    public List<User> getAllCustomers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_CUSTOMER")))
                .toList();
    }
    
    public long getTotalCustomers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_CUSTOMER")))
                .count();
    }
    
    private void sendRegistrationEmail(User user, String accountType) {
        try {
            String title = "Welcome to Online Pharmacy Management System!";
            String content = "Dear " + user.getFullName() + ",<br><br>" +
                           "Your account has been successfully created as a " + accountType + ".<br><br>" +
                           "You can now login and start using our services.<br><br>" +
                           "Your registered email is: <strong>" + user.getEmail() + "</strong>";
            
            java.util.Map<String, String> details = new java.util.HashMap<>();
            details.put("Account Type", accountType);
            details.put("Email", user.getEmail());
            details.put("Registration Date", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            
            String htmlContent = emailService.buildEmailTemplate(title, content, details);
            emailService.sendHtmlEmail(user.getEmail(), "Welcome to Pharmacy Management System", 
                                     htmlContent, "REGISTRATION", user.getId(), null);
        } catch (Exception e) {
            System.err.println("Failed to send registration email: " + e.getMessage());
        }
    }
}
