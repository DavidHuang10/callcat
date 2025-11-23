package com.callcat.backend.repository.dynamo;

import com.callcat.backend.entity.dynamo.UserPreferencesDynamoDb;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class UserPreferencesRepositoryDynamoDb {

    private final DynamoDbTable<UserPreferencesDynamoDb> table;

    public UserPreferencesRepositoryDynamoDb(DynamoDbEnhancedClient dynamoDb) {
        this.table = dynamoDb.table("callcat-user-preferences", TableSchema.fromBean(UserPreferencesDynamoDb.class));
    }

    public void save(UserPreferencesDynamoDb preferences) {
        table.putItem(preferences);
    }

    public Optional<UserPreferencesDynamoDb> findByEmail(String email) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(email).build()));
    }

    public void delete(UserPreferencesDynamoDb preferences) {
        table.deleteItem(preferences);
    }
    
    public void deleteByEmail(String email) {
        table.deleteItem(Key.builder().partitionValue(email).build());
    }
}
