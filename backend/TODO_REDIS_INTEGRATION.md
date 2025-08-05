# TODO: Redis Integration for Token Blacklist

## Current Implementation
- In-memory token blacklist using `ConcurrentHashMap`
- Tokens are blacklisted for 24 hours (matching JWT expiration)
- Automatic cleanup of expired tokens during checks

## Production Redis Integration Plan

### 1. Add Redis Dependencies
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 2. Redis Configuration
```properties
# Add to application.properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=your_redis_password
spring.redis.database=0
```

### 3. Update TokenBlacklistService
```java
@Service
public class TokenBlacklistService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public void blacklistToken(String token) {
        // Set token with 24-hour TTL
        redisTemplate.opsForValue().set(
            "blacklist:" + token, 
            "1", 
            Duration.ofHours(24)
        );
    }
    
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
    
    // No need for manual cleanup - Redis handles TTL automatically
}
```

### 4. Benefits of Redis Implementation
- **Automatic TTL**: Redis automatically removes expired tokens
- **Scalability**: Can handle millions of blacklisted tokens
- **Persistence**: Survives application restarts
- **Performance**: O(1) lookup time
- **Memory efficient**: Only stores active blacklisted tokens

### 5. Alternative NoSQL Options
- **MongoDB**: With TTL indexes
- **Cassandra**: With TTL columns
- **DynamoDB**: With TTL attributes
- **Elasticsearch**: With TTL field

### 6. Monitoring
- Track blacklist size
- Monitor Redis memory usage
- Set up alerts for high blacklist growth

## Implementation Priority
1. ✅ In-memory implementation (current)
2. 🔄 Redis integration (when scaling)
3. 📊 Monitoring and alerts
4. 🚀 Performance optimization

## Notes
- Current in-memory implementation is sufficient for development and small-scale production
- Switch to Redis when you have >1000 concurrent users or need horizontal scaling
- Redis TTL automatically handles cleanup, no manual intervention needed 