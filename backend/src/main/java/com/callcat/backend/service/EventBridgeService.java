package com.callcat.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class EventBridgeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventBridgeService.class);
    
    private final EventBridgeClient eventBridgeClient;
    
    @Value("${lambda.function.arn}")
    private String lambdaFunctionArn;
    
    public EventBridgeService(EventBridgeClient eventBridgeClient) {
        this.eventBridgeClient = eventBridgeClient;
    }
    
    public void scheduleCall(String callId, Long scheduledForMs) {
        try {
            String ruleName = "CallCat-" + callId;
            String cronExpression = createCronExpression(scheduledForMs);
            
            PutRuleRequest putRuleRequest = PutRuleRequest.builder()
                    .name(ruleName)
                    .scheduleExpression(cronExpression)
                    .state(RuleState.ENABLED)
                    .description("Scheduled call trigger for callId: " + callId)
                    .build();
            
            eventBridgeClient.putRule(putRuleRequest);
            
            Target target = Target.builder()
                    .id("1")
                    .arn(lambdaFunctionArn)
                    .input("{\"detail\":{\"callId\":\"" + callId + "\"}}")
                    .build();
            
            PutTargetsRequest putTargetsRequest = PutTargetsRequest.builder()
                    .rule(ruleName)
                    .targets(target)
                    .build();
            
            eventBridgeClient.putTargets(putTargetsRequest);
            
            logger.info("Successfully scheduled call {} for {}", callId, 
                    Instant.ofEpochMilli(scheduledForMs).toString());
            
        } catch (Exception e) {
            logger.error("Failed to schedule call {}: {}", callId, e.getMessage());
        }
    }
    
    private String createCronExpression(Long scheduledForMs) {
        Instant instant = Instant.ofEpochMilli(scheduledForMs);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm HH dd MM ? yyyy")
                .withZone(ZoneId.of("UTC"));
        return "cron(" + formatter.format(instant) + ")";
    }
}