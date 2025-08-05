# API Test Calls for Database Verification

## Prerequisites
1. Start the backend server: `cd backend && ./mvnw spring-boot:run`
2. Server should be running on http://localhost:8080

## 1. User Registration (PostgreSQL Test)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "TestPassword123!"
  }'
```

**Expected Response:** 
- Success: `{"message": "User registered successfully. Please verify your email."}`
- Check PostgreSQL: User should be created in `users` table

## 2. Email Verification (PostgreSQL Test)

```bash
curl -X POST http://localhost:8080/api/auth/send-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

**Expected Response:** `{"message": "Verification code sent to email"}`

**Check logs for verification code, then:**

```bash
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "verificationCode": "REPLACE_WITH_CODE_FROM_LOGS"
  }'
```

## 3. User Login (PostgreSQL + JWT Test)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPassword123!"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJ...",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "name": "Test User",
    "email": "test@example.com",
    "emailVerified": true
  }
}
```

**Save the JWT token for next steps!**

## 4. Get User Profile (Protected Endpoint Test)

```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Expected Response:** User profile data from PostgreSQL

## 5. Logout (DynamoDB Blacklist Test)

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Expected Response:** `{"message": "Logged out successfully"}`
**Check DynamoDB:** Token should appear in `callcat-blacklist` table

## 6. Try Using Blacklisted Token (DynamoDB Verification)

```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_BLACKLISTED_TOKEN_HERE"
```

**Expected Response:** `401 Unauthorized` - Token should be rejected

## 7. Update User Preferences (PostgreSQL Test)

First login again to get a fresh token, then:

```bash
curl -X PUT http://localhost:8080/api/user/preferences \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_FRESH_JWT_TOKEN_HERE" \
  -d '{
    "timezone": "America/New_York",
    "emailNotifications": false,
    "voiceId": "test-voice-123",
    "systemPrompt": "Be helpful and concise"
  }'
```

**Expected Response:** Updated preferences
**Check PostgreSQL:** `user_preferences` table should be updated

## Database Verification Commands

### PostgreSQL Verification
```sql
-- Connect to your RDS instance and run:
SELECT * FROM users;
SELECT * FROM user_preferences;
SELECT * FROM email_verifications;
```

### DynamoDB Verification
Check AWS Console -> DynamoDB -> Tables -> `callcat-blacklist`:
- Should see blacklisted tokens with `expiresAt` timestamps
- Tokens should auto-delete after 24 hours due to TTL

## Troubleshooting

### Connection Issues
- **PostgreSQL errors:** Check `.env` file has correct `DB_PASSWORD`
- **DynamoDB errors:** Ensure AWS CLI is configured (`aws configure`)
- **General errors:** Check server logs for detailed error messages

### Expected Behaviors
1. **Registration:** Creates user in PostgreSQL
2. **Login:** Validates against PostgreSQL, returns JWT
3. **Protected endpoints:** Validate JWT and check blacklist in DynamoDB
4. **Logout:** Adds token to DynamoDB blacklist with TTL
5. **Preferences:** Updates PostgreSQL user_preferences table