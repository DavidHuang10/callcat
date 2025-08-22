package com.callcat.backend.repository;

import com.callcat.backend.entity.CallRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CallRecordRepository {

    private final DynamoDbTable<CallRecord> table;
    private final DynamoDbIndex<CallRecord> byCallIdIndex;
    private final DynamoDbIndex<CallRecord> byProviderIndex;
    private final DynamoDbIndex<CallRecord> byUserStatusIndex;

    @Autowired
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

    public List<CallRecord> findByUserIdAndStatus(String userId, String status, Integer limit) {
        if ("SCHEDULED".equals(status)) {
            return findScheduledCallsByUserId(userId, limit);
        } else if ("COMPLETED".equals(status)) {
            return findCompletedCallsByUserId(userId, limit);
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
    

}