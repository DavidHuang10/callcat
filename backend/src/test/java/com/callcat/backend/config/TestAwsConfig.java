package com.callcat.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import com.callcat.backend.model.TokenBlacklist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration that provides mock AWS services for testing.
 * This prevents tests from trying to connect to real AWS services.
 */
@TestConfiguration
public class TestAwsConfig {
    
    /**
     * Provides a mock SesClient for tests.
     * The @Primary annotation ensures this mock is used instead of the real SesClient.
     */
    @Bean
    @Primary
    public SesClient mockSesClient() {
        SesClient mockSesClient = mock(SesClient.class);
        
        // Mock the sendEmail method to return a fake response
        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("test-message-id-12345")
                .build();
        
        when(mockSesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(mockResponse);
        
        return mockSesClient;
    }
    
    /**
     * Provides a mock DynamoDbClient for tests.
     * Prevents tests from connecting to real DynamoDB.
     */
    @Bean
    @Primary
    public DynamoDbClient mockDynamoDbClient() {
        return mock(DynamoDbClient.class);
    }
    
    /**
     * Provides a mock DynamoDbEnhancedClient for tests.
     * Prevents tests from connecting to real DynamoDB.
     */
    @Bean
    @Primary 
    public DynamoDbEnhancedClient mockDynamoDbEnhancedClient() {
        DynamoDbEnhancedClient mockClient = mock(DynamoDbEnhancedClient.class);
        
        // Mock the table method to return a mock table
        DynamoDbTable<TokenBlacklist> mockTable = mock(DynamoDbTable.class);
        when(mockClient.table(any(String.class), any(TableSchema.class)))
                .thenReturn(mockTable);
        
        return mockClient;
    }
}