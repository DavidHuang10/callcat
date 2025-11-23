package com.callcat.backend.repository;

import com.callcat.backend.entity.CallRecord;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CallRecordRepository {

    private final DynamoDbTable<CallRecord> table;
    private final DynamoDbIndex<CallRecord> byCallIdIndex;
    private final DynamoDbIndex<CallRecord> byProviderIndex;
    private final DynamoDbIndex<CallRecord> byUserStatusIndex;

    public CallRecordRepository(DynamoDbEnhancedClient dynamoDb) {
        this.table = dynamoDb.table("callcat-calls", TableSchema.fromBean(CallRecord.class));
        this.byCallIdIndex = table.index("byCallId");
        this.byProviderIndex = table.index("byProvider");
        this.byUserStatusIndex = table.index("byUserStatus");
    }

    public CallRecord save(CallRecord callRecord) {
        table.putItem(callRecord);
        return callRecord;
    }

    public List<CallRecord> findScheduledCallsByUserId(String userId, Integer limit) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId + "#SCHEDULED").build());
        
        return byUserStatusIndex.query(r -> r.queryConditional(queryConditional)
                .scanIndexForward(true) // Ascending order - soonest first
                .limit(limit != null ? limit : 20))
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public List<CallRecord> findCompletedCallsByUserId(String userId, Integer limit) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId + "#COMPLETED").build());
        
        return byUserStatusIndex.query(r -> r.queryConditional(queryConditional)
                .scanIndexForward(false) // Descending order - most recent first
                .limit(limit != null ? limit : 20))
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public List<CallRecord> findFailedCallsByUserId(String userId, Integer limit) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId + "#FAILED").build());
        
        return byUserStatusIndex.query(r -> r.queryConditional(queryConditional)
                .scanIndexForward(false) // Descending order - most recent first
                .limit(limit != null ? limit : 20))
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public List<CallRecord> findByUserIdAndStatus(String userId, String status, Integer limit) {
        if ("SCHEDULED".equals(status)) {
            return findScheduledCallsByUserId(userId, limit);
        } else if ("COMPLETED".equals(status)) {
            return findCompletedCallsByUserId(userId, limit);
        } else if ("FAILED".equals(status)) {
            return findFailedCallsByUserId(userId, limit);
        } else {
            return List.of();
        }
    }

    public void delete(CallRecord callRecord) {
        table.deleteItem(Key.builder()
                .partitionValue(callRecord.getUserId())
                .sortValue(callRecord.getSk())
                .build());
    }

    public Optional<CallRecord> findByCallId(String callId) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(callId).build());
        
        return byCallIdIndex.query(r -> r.queryConditional(queryConditional))
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }

    public Optional<CallRecord> findByProviderId(String providerId) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(providerId).build());

        return byProviderIndex.query(r -> r.queryConditional(queryConditional))
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }

    /**
     * Find all calls with specific status across all users.
     * This method uses a table scan which is expensive - use sparingly for admin operations only.
     */
    public List<CallRecord> findAllByStatus(String status) {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                        .expression("#status = :status")
                        .putExpressionName("#status", "status")
                        .putExpressionValue(":status", AttributeValue.builder().s(status).build())
                        .build())
                .build();

        return table.scan(scanRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Find scheduled calls that are overdue (scheduledFor before threshold).
     * Optimized with filter expression to reduce data transfer.
     */
    public List<CallRecord> findOverdueScheduledCalls(long thresholdTimestamp) {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                        .expression("#status = :status AND #scheduledFor < :threshold")
                        .putExpressionName("#status", "status")
                        .putExpressionName("#scheduledFor", "scheduledFor")
                        .putExpressionValue(":status", AttributeValue.builder().s("SCHEDULED").build())
                        .putExpressionValue(":threshold", AttributeValue.builder().n(String.valueOf(thresholdTimestamp)).build())
                        .build())
                .limit(100) // Limit to prevent excessive scans
                .build();

        return table.scan(scanRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }
}