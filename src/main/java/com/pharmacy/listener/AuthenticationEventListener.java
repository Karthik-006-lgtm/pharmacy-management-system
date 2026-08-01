package com.pharmacy.listener;

import com.pharmacy.entity.User;
import com.pharmacy.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AuthenticationEventListener {
    
    private final UserRepository userRepository;
    
    public AuthenticationEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setIsOnline(true);
                user.setLastSeenAt(LocalDateTime.now());
                userRepository.save(user);
            }
        }
    }
}
