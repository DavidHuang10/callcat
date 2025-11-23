package com.callcat.backend.service;

import com.callcat.backend.entity.User;
import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.repository.dynamo.UserRepositoryDynamoDb;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepositoryDynamoDb userRepository;
    
    public UserDetailsServiceImpl(UserRepositoryDynamoDb userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String lowerCaseEmail = email.toLowerCase();
        logger.debug("Loading user by username (email): {}", lowerCaseEmail);
        
        UserDynamoDb userDynamo = userRepository.findByEmail(lowerCaseEmail)
                .orElseThrow(() -> {
                    logger.error("User not found in DynamoDB: {}", lowerCaseEmail);
                    return new UsernameNotFoundException("User not found: " + lowerCaseEmail);
                });
                
        if (!Boolean.TRUE.equals(userDynamo.getIsActive())) {
             logger.warn("User found but not active: {}", lowerCaseEmail);
             throw new UsernameNotFoundException("User not active: " + email);
        }

        // Map DynamoDB entity to User domain object (which implements UserDetails)
        User user = new User();
        user.setEmail(userDynamo.getEmail());
        user.setPassword(userDynamo.getPassword());
        user.setFirstName(userDynamo.getFirstName());
        user.setLastName(userDynamo.getLastName());
        user.setRole(userDynamo.getRole());
        user.setIsActive(userDynamo.getIsActive());
        // ID is not available in DynamoDB, but we use email as identifier now
        
        logger.debug("User loaded successfully: {}", lowerCaseEmail);
        return user;
    }
}