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
    
    private final UserRepositoryDynamoDb userRepository;
    
    public UserDetailsServiceImpl(UserRepositoryDynamoDb userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String lowerCaseEmail = email.toLowerCase();
        UserDynamoDb userDynamo = userRepository.findByEmail(lowerCaseEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + lowerCaseEmail));
                
        if (!Boolean.TRUE.equals(userDynamo.getIsActive())) {
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
        
        return user;
    }
}