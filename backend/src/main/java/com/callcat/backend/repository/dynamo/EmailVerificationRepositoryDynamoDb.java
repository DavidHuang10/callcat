package com.callcat.backend.repository.dynamo;

import com.callcat.backend.entity.dynamo.EmailVerificationDynamoDb;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class EmailVerificationRepositoryDynamoDb {

    private final DynamoDbTable<EmailVerificationDynamoDb> table;

    public EmailVerificationRepositoryDynamoDb(DynamoDbEnhancedClient dynamoDb) {
        this.table = dynamoDb.table("callcat-email-verifications", TableSchema.fromBean(EmailVerificationDynamoDb.class));
    }

    public void save(EmailVerificationDynamoDb verification) {
        table.putItem(verification);
    }

    public Optional<EmailVerificationDynamoDb> findByEmail(String email) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(email).build()));
    }

    public void delete(EmailVerificationDynamoDb verification) {
        table.deleteItem(verification);
    }
    
    public void deleteByEmail(String email) {
        table.deleteItem(Key.builder().partitionValue(email).build());
    }

    public void deleteExpiredVerifications(long currentTimeSeconds) {
        software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest scanRequest = 
            software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                        .expression("expiresAt < :now")
                        .putExpressionValue(":now", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().n(String.valueOf(currentTimeSeconds)).build())
                        .build())
                .build();

        for (EmailVerificationDynamoDb item : table.scan(scanRequest).items()) {
            table.deleteItem(item);
        }
    }
}
