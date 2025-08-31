package com.callcat.backend.repository;

import com.callcat.backend.entity.CallTranscript;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class CallTranscriptRepository {

    private final DynamoDbTable<CallTranscript> table;

    public CallTranscriptRepository(DynamoDbEnhancedClient dynamoDb) {
        this.table = dynamoDb.table("callcat-transcripts", TableSchema.fromBean(CallTranscript.class));
    }

    public CallTranscript save(CallTranscript transcript) {
        table.putItem(transcript);
        return transcript;
    }

    public Optional<CallTranscript> findByProviderId(String providerId) {
        CallTranscript result = table.getItem(Key.builder()
                .partitionValue(providerId)
                .build());
        return Optional.ofNullable(result);
    }

    public void deleteByProviderId(String providerId) {
        table.deleteItem(Key.builder()
                .partitionValue(providerId)
                .build());
    }
}