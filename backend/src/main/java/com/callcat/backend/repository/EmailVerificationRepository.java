package com.callcat.backend.repository;

import com.callcat.backend.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    
    Optional<EmailVerification> findByEmail(String email);
    
    Optional<EmailVerification> findByEmailAndVerified(String email, Boolean verified);
    
    /**
     * Delete expired verification records (cleanup)
     */
    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.expiresAt < :now")
    void deleteExpiredVerifications(@Param("now") LocalDateTime now);
    
    /**
     * Find verified email verification
     */
    @Query("SELECT e FROM EmailVerification e WHERE e.email = :email AND e.verified = true")
    Optional<EmailVerification> findVerifiedByEmail(@Param("email") String email);
}