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
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CallRecordRepository {

    private final DynamoDbTable<CallRecord> table;
    private final DynamoDbIndex<CallRecord> upcomingCallsIndex;
    private final DynamoDbIndex<CallRecord> completedCallsIndex;

    @Autowired
    public CallRecordRepository(DynamoDbEnhancedClient dynamoDb) {
        this.table = dynamoDb.table("callcat-calls", TableSchema.fromBean(CallRecord.class));
        this.upcomingCallsIndex = table.index("upcoming-calls-index");
        this.completedCallsIndex = table.index("completed-calls-index");
    }

    public CallRecord save(CallRecord callRecord) {
        table.putItem(callRecord);
        return callRecord;
    }

    public Optional<CallRecord> findByUserIdAndCallId(Long userId, String callId) {
        CallRecord result = table.getItem(Key.builder()
                .partitionValue(userId)
                .sortValue(callId)
                .build());
        return Optional.ofNullable(result);
    }

    public List<CallRecord> findUpcomingCallsByUserId(Long userId, Integer limit) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build());
        
        return upcomingCallsIndex.query(r -> r.queryConditional(queryConditional)
                .limit(limit != null ? limit : 20))
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public List<CallRecord> findCompletedCallsByUserId(Long userId, Integer limit) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build());
        
        return completedCallsIndex.query(r -> r.queryConditional(queryConditional)
                .scanIndexForward(false)
                .limit(limit != null ? limit : 20))
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public List<CallRecord> findByUserIdAndStatus(Long userId, String status, Integer limit) {
        if ("SCHEDULED".equals(status) || "IN_PROGRESS".equals(status)) {
            return findUpcomingCallsByUserId(userId, limit).stream()
                    .filter(call -> status.equals(call.getStatus()))
                    .collect(Collectors.toList());
        } else if ("COMPLETED".equals(status)) {
            return findCompletedCallsByUserId(userId, limit);
        } else {
            return List.of();
        }
    }

    public void delete(CallRecord callRecord) {
        table.deleteItem(Key.builder()
                .partitionValue(callRecord.getUserId())
                .sortValue(callRecord.getCallId())
                .build());
    }
}