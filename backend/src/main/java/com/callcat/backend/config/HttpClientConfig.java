package com.callcat.backend.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;

/**
 * HTTP client configuration to prevent memory leaks by properly managing connection pools.
 * 
 * This configuration addresses the metaspace memory leak issue by:
 * 1. Reusing HTTP connections instead of creating new ones for each request
 * 2. Limiting the total number of connections to prevent resource exhaustion  
 * 3. Setting appropriate timeouts to prevent hanging connections
 * 4. Ensuring connections are properly closed and recycled
 */
@Configuration
public class HttpClientConfig {
    
    @Bean
    public PoolingHttpClientConnectionManager connectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        
        // Connection pool settings optimized for t3.micro (1GB RAM)
        connectionManager.setMaxTotal(20); // Maximum total connections across all routes
        connectionManager.setDefaultMaxPerRoute(10); // Maximum connections per route (e.g., to retell.ai)
        connectionManager.setValidateAfterInactivity(TimeValue.ofSeconds(10)); // Validate connections after 10s idle
        
        // Connection timeout settings
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(30)) // Connection establishment timeout
            .setSocketTimeout(Timeout.ofSeconds(60))  // Socket read timeout
            .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);
        
        return connectionManager;
    }
    
    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(30)) // Timeout when requesting connection from pool
            .setResponseTimeout(Timeout.ofSeconds(60))           // Response timeout
            .build();
            
        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .evictIdleConnections(TimeValue.ofMinutes(2)) // Close idle connections after 2 minutes
            .evictExpiredConnections() // Automatically remove expired connections
            .build();
    }
    
    @Bean
    public HttpComponentsClientHttpRequestFactory httpRequestFactory(CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        
        // Additional request timeout settings
        factory.setConnectTimeout(Duration.ofSeconds(30));
        factory.setConnectionRequestTimeout(Duration.ofSeconds(30));
        
        return factory;
    }
}