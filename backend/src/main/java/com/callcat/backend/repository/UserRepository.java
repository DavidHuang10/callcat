package com.callcat.backend.repository;

import com.callcat.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    Optional<User> findByEmailAndIsActive(String email, Boolean isActive);
    
    Optional<User> findByPasswordResetToken(String passwordResetToken);
    
}