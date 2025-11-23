package com.callcat.backend.repository.dynamo;

import com.callcat.backend.entity.dynamo.UserDynamoDb;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class UserRepositoryDynamoDb {

    private final DynamoDbTable<UserDynamoDb> table;

    public UserRepositoryDynamoDb(DynamoDbEnhancedClient dynamoDb) {
        this.table = dynamoDb.table("callcat-users", TableSchema.fromBean(UserDynamoDb.class));
    }

    public void save(UserDynamoDb user) {
        table.putItem(user);
    }

    public Optional<UserDynamoDb> findByEmail(String email) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(email).build()));
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public void delete(UserDynamoDb user) {
        table.deleteItem(user);
    }
    
    public void deleteByEmail(String email) {
        table.deleteItem(Key.builder().partitionValue(email).build());
    }

    public Optional<UserDynamoDb> findByPasswordResetToken(String token) {
        software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex<UserDynamoDb> index = table.index("byResetToken");
        software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional queryConditional = 
            software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo(
                Key.builder().partitionValue(token).build());
        
        return index.query(r -> r.queryConditional(queryConditional))
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }
}
