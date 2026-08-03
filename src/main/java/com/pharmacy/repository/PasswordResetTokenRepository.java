package com.pharmacy.repository;

import com.pharmacy.entity.PasswordResetToken;
import com.pharmacy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, LocalDateTime currentTime);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < ?1")
    void deleteExpiredTokens(LocalDateTime currentTime);
    
    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.user = ?1 AND p.createdAt > ?2")
    long countRecentTokensByUser(User user, LocalDateTime since);
    
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user = ?1 AND p.used = false")
    void invalidateAllUserTokens(User user);
    
    @Query("SELECT p FROM PasswordResetToken p WHERE p.user = ?1 AND p.used = false AND p.expiryDate > ?2")
    List<PasswordResetToken> findActiveTokensByUser(User user, LocalDateTime currentTime);
}
