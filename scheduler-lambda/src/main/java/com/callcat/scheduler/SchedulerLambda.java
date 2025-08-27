package com.callcat.scheduler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;


import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DeleteRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.RemoveTargetsRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class SchedulerLambda implements RequestHandler<ScheduledEvent, String> {

    private final EventBridgeClient eventBridgeClient;
    private final HttpClient httpClient;
    private final String springBootUrl;

    public SchedulerLambda() {
        this.eventBridgeClient = EventBridgeClient.create();
        this.httpClient = HttpClient.newHttpClient();
        this.springBootUrl = System.getenv("SPRING_BOOT_BASE_URL");
    }

    @Override
    public String handleRequest(ScheduledEvent event, Context context) {
        try {
            // Extract callId from event detail
            Map<String, Object> detail = event.getDetail();
            String callId = (String) detail.get("callId");
            
            context.getLogger().log("Processing scheduled call: " + callId);

            // 1. Trigger call via Spring Boot API (includes validation)
            boolean success = triggerCall(callId);
            if (!success) {
                context.getLogger().log("Failed to trigger call: " + callId);
                return "Failed to trigger call";
            }

            // 2. Clean up EventBridge rule
            cleanupEventBridgeRule("CallCat-" + callId);

            context.getLogger().log("Successfully processed call: " + callId);
            return "Success";

        } catch (Exception e) {
            context.getLogger().log("Error processing scheduled call: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }


    private boolean triggerCall(String callId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(springBootUrl + "/api/calls/" + callId + "/trigger"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json")
                .header("X-API-Key", System.getenv("LAMBDA_API_KEY"))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            throw new RuntimeException("Failed to trigger call", e);
        }
    }

    private void cleanupEventBridgeRule(String ruleName) {
        try {
            // Remove targets first
            RemoveTargetsRequest removeTargetsRequest = RemoveTargetsRequest.builder()
                .rule(ruleName)
                .ids("1")
                .build();
            eventBridgeClient.removeTargets(removeTargetsRequest);

            // Delete rule
            DeleteRuleRequest deleteRuleRequest = DeleteRuleRequest.builder()
                .name(ruleName)
                .build();
            eventBridgeClient.deleteRule(deleteRuleRequest);
        } catch (Exception e) {
            // Log but don't fail - cleanup is best effort
            System.err.println("Failed to cleanup EventBridge rule: " + e.getMessage());
        }
    }

}
