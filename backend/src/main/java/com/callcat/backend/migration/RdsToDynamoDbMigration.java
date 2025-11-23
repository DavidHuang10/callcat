package com.callcat.backend.migration;

import com.callcat.backend.entity.EmailVerification;
import com.callcat.backend.entity.User;
import com.callcat.backend.entity.UserPreferences;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.dynamo.EmailVerificationDynamoDb;
import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.entity.dynamo.UserPreferencesDynamoDb;
import com.callcat.backend.repository.CallRecordRepository;
import com.callcat.backend.repository.EmailVerificationRepository;
import com.callcat.backend.repository.UserPreferencesRepository;
import com.callcat.backend.repository.UserRepository;
import com.callcat.backend.repository.dynamo.EmailVerificationRepositoryDynamoDb;
import com.callcat.backend.repository.dynamo.UserPreferencesRepositoryDynamoDb;
import com.callcat.backend.repository.dynamo.UserRepositoryDynamoDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "migration.rds-to-dynamo.enabled", havingValue = "true")
public class RdsToDynamoDbMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RdsToDynamoDbMigration.class);

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    
    private final UserRepositoryDynamoDb userRepositoryDynamoDb;
    private final EmailVerificationRepositoryDynamoDb emailVerificationRepositoryDynamoDb;
    private final UserPreferencesRepositoryDynamoDb userPreferencesRepositoryDynamoDb;
    private final CallRecordRepository callRecordRepository;

    public RdsToDynamoDbMigration(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository,
            UserPreferencesRepository userPreferencesRepository,
            UserRepositoryDynamoDb userRepositoryDynamoDb,
            EmailVerificationRepositoryDynamoDb emailVerificationRepositoryDynamoDb,
            UserPreferencesRepositoryDynamoDb userPreferencesRepositoryDynamoDb,
            CallRecordRepository callRecordRepository) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.userRepositoryDynamoDb = userRepositoryDynamoDb;
        this.emailVerificationRepositoryDynamoDb = emailVerificationRepositoryDynamoDb;
        this.userPreferencesRepositoryDynamoDb = userPreferencesRepositoryDynamoDb;
        this.callRecordRepository = callRecordRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) throws Exception {
        logger.info("Starting RDS to DynamoDB migration...");

        List<User> users = userRepository.findAll();
        logger.info("Found {} users to migrate", users.size());

        for (User user : users) {
            try {
                migrateUser(user);
            } catch (Exception e) {
                logger.error("Failed to migrate user: {}", user.getEmail(), e);
            }
        }

        logger.info("Migration completed.");
    }

    private void migrateUser(User user) {
        logger.info("Migrating user: {}", user.getEmail());

        // 1. Migrate User
        UserDynamoDb userDynamo = new UserDynamoDb();
        userDynamo.setEmail(user.getEmail());
        userDynamo.setPassword(user.getPassword());
        userDynamo.setFirstName(user.getFirstName());
        userDynamo.setLastName(user.getLastName());
        userDynamo.setRole(user.getRole());
        userDynamo.setIsActive(user.getIsActive());
        userDynamo.setPasswordResetToken(user.getPasswordResetToken());
        userDynamo.setResetTokenExpires(user.getResetTokenExpires());
        if (user.getCreatedAt() != null) {
            userDynamo.setCreatedAt(user.getCreatedAt().toString());
        }
        if (user.getUpdatedAt() != null) {
            userDynamo.setUpdatedAt(user.getUpdatedAt().toString());
        }
        userRepositoryDynamoDb.save(userDynamo);

        // 2. Migrate EmailVerification
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findById(user.getEmail());
        if (verificationOpt.isPresent()) {
            EmailVerification verification = verificationOpt.get();
            EmailVerificationDynamoDb verificationDynamo = new EmailVerificationDynamoDb();
            verificationDynamo.setEmail(verification.getEmail());
            verificationDynamo.setVerificationCode(verification.getVerificationCode());
            verificationDynamo.setExpiresAt(verification.getExpiresAt());
            verificationDynamo.setVerified(verification.getVerified());
            verificationDynamo.setCreatedAt(verification.getCreatedAt());
            emailVerificationRepositoryDynamoDb.save(verificationDynamo);
        }

        // 3. Migrate UserPreferences
        Optional<UserPreferences> preferencesOpt = userPreferencesRepository.findByUserId(user.getId());
        if (preferencesOpt.isPresent()) {
            UserPreferences preferences = preferencesOpt.get();
            UserPreferencesDynamoDb preferencesDynamo = new UserPreferencesDynamoDb();
            preferencesDynamo.setEmail(user.getEmail()); // Use email as PK
            preferencesDynamo.setTimezone(preferences.getTimezone());
            preferencesDynamo.setEmailNotifications(preferences.getEmailNotifications());
            preferencesDynamo.setVoiceId(preferences.getVoiceId());
            preferencesDynamo.setSystemPrompt(preferences.getSystemPrompt());
            userPreferencesRepositoryDynamoDb.save(preferencesDynamo);
        }
        
        // 4. Migrate CallRecords (Update userId from ID to Email)
        migrateCallRecords(user);
    }
    
    private void migrateCallRecords(User user) {
        String oldUserId = user.getId().toString();
        String newUserId = user.getEmail();
        
        // Find all scheduled calls
        List<CallRecord> scheduledCalls = callRecordRepository.findByUserIdAndStatus(oldUserId, "SCHEDULED", 1000);
        for (CallRecord call : scheduledCalls) {
            migrateCallRecord(call, newUserId);
        }
        
        // Find all completed calls
        List<CallRecord> completedCalls = callRecordRepository.findByUserIdAndStatus(oldUserId, "COMPLETED", 1000);
        for (CallRecord call : completedCalls) {
            migrateCallRecord(call, newUserId);
        }

        // Find all failed calls
        List<CallRecord> failedCalls = callRecordRepository.findByUserIdAndStatus(oldUserId, "FAILED", 1000);
        for (CallRecord call : failedCalls) {
            migrateCallRecord(call, newUserId);
        }
    }
    
    private void migrateCallRecord(CallRecord oldCall, String newUserId) {
        // Create new call record with new userId
        CallRecord newCall = new CallRecord();
        
        // Copy all properties
        newCall.setUserId(newUserId); // NEW USER ID
        newCall.setCallId(oldCall.getCallId());
        newCall.setCalleeName(oldCall.getCalleeName());
        newCall.setPhoneNumber(oldCall.getPhoneNumber());
        newCall.setCallerNumber(oldCall.getCallerNumber());
        newCall.setSubject(oldCall.getSubject());
        newCall.setPrompt(oldCall.getPrompt());
        newCall.setStatus(oldCall.getStatus());
        newCall.setScheduledFor(oldCall.getScheduledFor());
        newCall.setAiLanguage(oldCall.getAiLanguage());
        newCall.setVoiceId(oldCall.getVoiceId());
        newCall.setCreatedAt(oldCall.getCreatedAt());
        newCall.setUpdatedAt(oldCall.getUpdatedAt());
        newCall.setProviderId(oldCall.getProviderId());
        newCall.setDialSuccessful(oldCall.getDialSuccessful());
        newCall.setCompletedAt(oldCall.getCompletedAt());
        newCall.setCallAnalyzed(oldCall.getCallAnalyzed());
        newCall.setRetellCallData(oldCall.getRetellCallData());
        
        // Save new record
        callRecordRepository.save(newCall);
        
        // Delete old record
        callRecordRepository.delete(oldCall);
        
        logger.info("Migrated call {} from user {} to {}", oldCall.getCallId(), oldCall.getUserId(), newUserId);
    }
}
