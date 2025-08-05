package com.callcat.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

// Test configuration for DynamoDB integration tests
// Configures DynamoDB client to connect to local DynamoDB instance for testing
// This allows testing actual DynamoDB operations without requiring AWS resources
@TestConfiguration
@Profile("test")
public class TestDynamoDbConfig {

    /**
     * Creates a DynamoDB client configured for local testing
     * Points to localhost:8000 where DynamoDB Local should be running
     * Uses dummy credentials since DynamoDB Local doesn't require real AWS credentials
     */
    @Bean
    @Primary
    public DynamoDbClient testDynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:8000"))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build();
    }

    /**
     * Creates an enhanced DynamoDB client for high-level operations
     * Uses the test DynamoDB client configured above
     */
    @Bean
    @Primary
    public DynamoDbEnhancedClient testDynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}