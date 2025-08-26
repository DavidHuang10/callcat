package com.callcat.scheduler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DeleteRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.RemoveTargetsRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class SchedulerLambda implements RequestHandler<ScheduledEvent, String> {

    private final DynamoDbClient dynamoClient;
    private final EventBridgeClient eventBridgeClient;
    private final HttpClient httpClient;
    private final String springBootUrl;
    private final String callsTableName;

    public SchedulerLambda() {
        this.dynamoClient = DynamoDbClient.create();
        this.eventBridgeClient = EventBridgeClient.create();
        this.httpClient = HttpClient.newHttpClient();
        this.springBootUrl = System.getenv("SPRING_BOOT_BASE_URL");
        this.callsTableName = System.getenv("DYNAMODB_CALLS_TABLE");
    }

    @Override
    public String handleRequest(ScheduledEvent event, Context context) {
        try {
            // Extract callId from event detail
            Map<String, Object> detail = event.getDetail();
            String callId = (String) detail.get("callId");
            
            context.getLogger().log("Processing scheduled call: " + callId);

            // 1. Query DynamoDB to get call details
            CallRecord callRecord = getCallFromDynamoDB(callId);
            if (callRecord == null || !"SCHEDULED".equals(callRecord.status)) {
                context.getLogger().log("Call not found or not scheduled: " + callId);
                return "Call not found or not scheduled";
            }

            // 2. Trigger call via Spring Boot API
            boolean success = triggerCall(callId);
            if (!success) {
                context.getLogger().log("Failed to trigger call: " + callId);
                return "Failed to trigger call";
            }

            // 3. Clean up EventBridge rule
            cleanupEventBridgeRule("CallCat-" + callId);

            context.getLogger().log("Successfully processed call: " + callId);
            return "Success";

        } catch (Exception e) {
            context.getLogger().log("Error processing scheduled call: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private CallRecord getCallFromDynamoDB(String callId) {
        try {
            QueryRequest request = QueryRequest.builder()
                .tableName(callsTableName)
                .indexName("byCallId")
                .keyConditionExpression("callId = :callId")
                .expressionAttributeValues(Map.of(":callId", AttributeValue.builder().s(callId).build()))
                .build();

            QueryResponse response = dynamoClient.query(request);
            if (response.items().isEmpty()) {
                return null;
            }

            Map<String, AttributeValue> item = response.items().get(0);
            CallRecord record = new CallRecord();
            record.callId = item.get("callId").s();
            record.status = item.get("status").s();
            record.userId = item.get("userId").s();
            
            return record;
        } catch (Exception e) {
            throw new RuntimeException("Failed to query DynamoDB", e);
        }
    }

    private boolean triggerCall(String callId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(springBootUrl + "/api/calls/" + callId + "/trigger"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json")
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

    private static class CallRecord {
        String callId;
        String status;
        String userId;
    }
}
