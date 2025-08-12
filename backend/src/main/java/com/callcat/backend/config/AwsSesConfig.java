package com.callcat.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsSesConfig {
    
    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.US_EAST_1)  // SES is available in us-east-1, us-west-2, eu-west-1
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}