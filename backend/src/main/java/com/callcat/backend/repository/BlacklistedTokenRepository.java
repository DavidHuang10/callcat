package com.callcat.backend.repository;

import com.callcat.backend.entity.BlacklistedToken;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class BlacklistedTokenRepository {

    private final DynamoDbTable<BlacklistedToken> table;

    public BlacklistedTokenRepository(DynamoDbEnhancedClient dynamoDb) {
        this.table = dynamoDb.table("callcat-blacklist", TableSchema.fromBean(BlacklistedToken.class));
    }

    public void save(BlacklistedToken token) {
        table.putItem(token);
    }

    public boolean exists(String token) {
        BlacklistedToken result = table.getItem(Key.builder()
                .partitionValue(token)
                .build());
        return result != null;
    }
}