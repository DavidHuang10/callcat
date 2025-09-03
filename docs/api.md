# CallCat API Documentation

## Base URL
- **Production**: `https://api.call-cat.com` (alias for `https://callcat-backend.us-east-1.elasticbeanstalk.com`)
- **Direct URL**: `https://callcat-backend.us-east-1.elasticbeanstalk.com`

## Authentication

CallCat uses JWT (JSON Web Token) authentication. Protected endpoints require a valid JWT token in the Authorization header.

### Authentication Header Format
```
Authorization: Bearer <jwt_token>
```

The JWT token is obtained through login and expires after 24 hours.

---

## Authentication Endpoints (Public)

### 1. Register User

**Endpoint**: `POST /api/auth/register`  
**Description**: Register a new user account. Email must be verified first via `/api/auth/verify-email`.  
**Authentication**: None required

#### Request Body
```json
{
  "email": "user@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Validation Rules
- `email`: Valid email format, required
- `password`: Minimum 8 characters, required
- `firstName`: 1-50 characters, required  
- `lastName`: 1-50 characters, required

#### Example cURL
```bash
curl -X POST https://api.call-cat.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "MySecurePassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### Success Response (200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123...",
  "type": "Bearer",
  "userId": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "expiresIn": 86400000
}
```

#### Error Response (400)
```json
{
  "message": "Email already registered",
  "success": false
}
```

---

### 2. Login User

**Endpoint**: `POST /api/auth/login`  
**Description**: Authenticate user and obtain JWT token  
**Authentication**: None required

#### Request Body
```json
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

#### Example cURL
```bash
curl -X POST https://api.call-cat.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "MySecurePassword123"
  }'
```

#### Success Response (200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123...",
  "type": "Bearer",
  "userId": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "expiresIn": 86400000
}
```

#### Error Response (400)
```json
{
  "message": "Invalid credentials",
  "success": false
}
```

---

### 3. Logout User

**Endpoint**: `POST /api/auth/logout`  
**Description**: Logout user and blacklist the JWT token  
**Authentication**: Bearer token required

#### Example cURL
```bash
curl -X POST https://api.call-cat.com/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..."
```

#### Success Response (200)
```json
{
  "message": "Successfully logged out",
  "success": true
}
```

#### Error Response (400)
```json
{
  "message": "Invalid token",
  "success": false
}
```

---

### 4. Send Email Verification

**Endpoint**: `POST /api/auth/send-verification`  
**Description**: Send verification code to email address for registration  
**Authentication**: None required

#### Request Body
```json
{
  "email": "user@example.com"
}
```

#### Example cURL
```bash
curl -X POST https://api.call-cat.com/api/auth/send-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'
```

#### Success Response (200)
```json
{
  "message": "Verification code sent to email",
  "success": true
}
```

#### Error Response (400)
```json
{
  "message": "Email already verified",
  "success": false
}
```

---

### 5. Verify Email

**Endpoint**: `POST /api/auth/verify-email`  
**Description**: Verify email address using the code sent via email  
**Authentication**: None required

#### Request Body
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

#### Example cURL
```bash
curl -X POST https://api.call-cat.com/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "code": "987654"
  }'
```

#### Success Response (200)
```json
{
  "message": "Email verified successfully",
  "success": true
}
```

#### Error Response (400)
```json
{
  "message": "Invalid verification code",
  "success": false
}
```

---

### 6. Forgot Password

**Endpoint**: `POST /api/auth/forgot-password`  
**Description**: Send password reset instructions to email  
**Authentication**: None required

#### Request Body
```json
{
  "email": "user@example.com"
}
```

#### Example cURL
```bash
curl -X POST https://api.call-cat.com/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'
```

#### Success Response (200)
```json
{
  "message": "Password reset instructions sent to email",
  "success": true
}
```

#### Error Response (400)
```json
{
  "message": "Email not found",
  "success": false
}
```

---

### 7. Reset Password

**Endpoint**: `POST /api/auth/reset-password`  
**Description**: Reset password using token from email  
**Authentication**: None required

#### Request Body
```json
{
  "token": "reset_token_from_email",
  "newPassword": "NewSecurePass123"
}
```

#### Validation Rules
- `token`: Required, from password reset email
- `newPassword`: Minimum 8 characters, required

#### Example cURL
```bash
curl -X POST https://api.call-cat.com/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "abc123def456ghi789",
    "newPassword": "MyNewSecurePassword123"
  }'
```

#### Success Response (200)
```json
{
  "message": "Password reset successfully",
  "success": true
}
```

#### Error Response (400)
```json
{
  "message": "Invalid or expired reset token",
  "success": false
}
```

---

## User Management Endpoints (Protected)

All user endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### 8. Get User Profile

**Endpoint**: `GET /api/user/profile`  
**Description**: Get current user's profile information  
**Authentication**: Bearer token required

#### Example cURL
```bash
curl -X GET https://api.call-cat.com/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..."
```

#### Success Response (200)
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "createdAt": "2023-10-01T10:30:00"
}
```

#### Error Response (401)
```json
{
  "message": "Unauthorized",
  "success": false
}
```

---

### 9. Update User Profile

**Endpoint**: `PUT /api/user/profile`  
**Description**: Update user's profile information (first name and last name)  
**Authentication**: Bearer token required

#### Request Body
```json
{
  "firstName": "John",
  "lastName": "Smith"
}
```

#### Validation Rules
- `firstName`: 1-50 characters, required
- `lastName`: 1-50 characters, required

#### Example cURL
```bash
curl -X PUT https://api.call-cat.com/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..." \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith"
  }'
```

#### Success Response (200)
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "fullName": "John Smith",
  "createdAt": "2023-10-01T10:30:00"
}
```

#### Error Response (400)
```json
{
  "message": "First name is required",
  "success": false
}
```

---

### 10. Change Password

**Endpoint**: `POST /api/user/change-password`  
**Description**: Change user's password  
**Authentication**: Bearer token required

#### Request Body
```json
{
  "currentPassword": "OldPassword123",
  "newPassword": "NewPassword123"
}
```

#### Validation Rules
- `currentPassword`: Required for verification
- `newPassword`: Minimum 8 characters, required

#### Example cURL
```bash
curl -X POST https://api.call-cat.com/api/user/change-password \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..." \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "MyCurrentPassword123",
    "newPassword": "MyNewPassword456"
  }'
```

#### Success Response (200)
```json
{
  "message": "Password changed successfully",
  "success": true
}
```

#### Error Response (400)
```json
{
  "message": "Current password is incorrect",
  "success": false
}
```

---

### 11. Get User Preferences

**Endpoint**: `GET /api/user/preferences`  
**Description**: Get current user's preferences  
**Authentication**: Bearer token required

#### Example cURL
```bash
curl -X GET https://api.call-cat.com/api/user/preferences \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..."
```

#### Success Response (200)
```json
{
  "timezone": "UTC",
  "emailNotifications": true,
  "voiceId": "voice_id_123",
  "systemPrompt": "You are a helpful AI assistant"
}
```

#### Error Response (401)
```json
{
  "message": "Unauthorized",
  "success": false
}
```

---

### 12. Update User Preferences

**Endpoint**: `PUT /api/user/preferences`  
**Description**: Update user's preferences  
**Authentication**: Bearer token required

#### Request Body
```json
{
  "timezone": "America/New_York",
  "emailNotifications": false,
  "voiceId": "new_voice_id_456",
  "systemPrompt": "Custom AI behavior instructions"
}
```

#### Validation Rules
- `systemPrompt`: Maximum 1000 characters
- All fields are optional

#### Example cURL
```bash
curl -X PUT https://api.call-cat.com/api/user/preferences \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..." \
  -H "Content-Type: application/json" \
  -d '{
    "timezone": "America/Los_Angeles",
    "emailNotifications": true,
    "voiceId": "voice_id_789",
    "systemPrompt": "Be friendly and professional in all interactions"
  }'
```

#### Success Response (200)
```json
{
  "timezone": "America/Los_Angeles",
  "emailNotifications": true,
  "voiceId": "voice_id_789",
  "systemPrompt": "Be friendly and professional in all interactions"
}
```

#### Error Response (400)
```json
{
  "message": "System prompt cannot exceed 1000 characters",
  "success": false
}
```

---

## Call Management Endpoints (Protected)

All call endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### 13. Create Call

**Endpoint**: `POST /api/calls`  
**Description**: Create a new call (immediate or scheduled)  
**Authentication**: Bearer token required

#### Request Body
```json
{
  "calleeName": "David Huang",
  "phoneNumber": "+19144919901",
  "subject": "hello to mom",
  "prompt": "say hello and ask how his day is",
  "scheduledFor": 1756819800000,
  "aiLanguage": "en",
  "voiceId": "voice_id_123"
}
```

#### Validation Rules
- `calleeName`: Required, max 100 characters
- `phoneNumber`: Required, E.164 format (+1XXXXXXXXXX)
- `subject`: Required, max 200 characters
- `prompt`: Required, max 5000 characters
- `scheduledFor`: Optional, Unix timestamp in milliseconds (future calls)
- `aiLanguage`: Optional, max 10 characters (default: "en")
- `voiceId`: Optional, max 100 characters

#### Example cURL (Immediate Call)
```bash
curl -X POST https://api.call-cat.com/api/calls \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..." \
  -H "Content-Type: application/json" \
  -d '{
    "calleeName": "Jane Doe",
    "phoneNumber": "+15555551234",
    "subject": "Appointment Confirmation",
    "prompt": "Please confirm your appointment for tomorrow at 3 PM",
    "aiLanguage": "en"
  }'
```

#### Example cURL (Scheduled Call)
```bash
curl -X POST https://api.call-cat.com/api/calls \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..." \
  -H "Content-Type: application/json" \
  -d '{
    "calleeName": "Robert Johnson",
    "phoneNumber": "+15555554321",
    "subject": "Follow-up Call",
    "prompt": "Follow up on our meeting from last week",
    "scheduledFor": 1735689600000,
    "aiLanguage": "en",
    "voiceId": "voice_id_456"
  }'
```

#### Success Response (200)
```json
{
  "callId": "550e8400-e29b-41d4-a716-446655440000",
  "calleeName": "Jane Doe",
  "phoneNumber": "+15555551234",
  "callerNumber": "+15551234567",
  "subject": "Appointment Confirmation",
  "prompt": "Please confirm your appointment for tomorrow at 3 PM",
  "status": "SCHEDULED",
  "scheduledFor": null,
  "providerId": null,
  "aiLanguage": "en",
  "voiceId": "voice_id_123",
  "createdAt": 1703980700000,
  "updatedAt": 1703980700000,
  "completedAt": null,
  "dialSuccessful": null,
  "callAnalyzed": false
}
```

#### Error Response (400)
```json
{
  "message": "Phone number must be in E.164 format (+1XXXXXXXXXX)",
  "success": false
}
```

---

### 14. Get Calls List

**Endpoint**: `GET /api/calls?status={status}&limit={limit}`  
**Description**: Get list of user's calls with filtering  
**Authentication**: Bearer token required

#### Query Parameters
- `status`: Optional, filter by call status ("SCHEDULED" or "COMPLETED")
- `limit`: Optional, max results (default: 20, max: 100)

#### Example cURL (All Scheduled Calls)
```bash
curl -X GET "https://api.call-cat.com/api/calls?status=SCHEDULED&limit=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..."
```

#### Example cURL (All Completed Calls)
```bash
curl -X GET "https://api.call-cat.com/api/calls?status=COMPLETED&limit=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..."
```

#### Success Response (200)
```json
{
  "calls": [
    {
      "callId": "550e8400-e29b-41d4-a716-446655440000",
      "calleeName": "Jane Doe",
      "phoneNumber": "+15555551234",
      "callerNumber": "+15551234567",
      "subject": "Appointment Confirmation",
      "prompt": "Please confirm your appointment for tomorrow at 3 PM",
      "status": "SCHEDULED",
      "scheduledFor": 1703980800000,
      "providerId": null,
      "aiLanguage": "en",
      "voiceId": "voice_id_123",
      "createdAt": 1703980700000,
      "updatedAt": 1703980700000,
      "completedAt": null,
      "dialSuccessful": null,
      "callAnalyzed": false
    }
  ]
}
```

#### Error Response (400)
```json
{
  "message": "Limit cannot exceed 100",
  "success": false
}
```

---

### 15. Get Single Call

**Endpoint**: `GET /api/calls/{callId}`  
**Description**: Get details of a specific call  
**Authentication**: Bearer token required

#### Path Parameters
- `callId`: UUID of the call

#### Example cURL
```bash
curl -X GET https://api.call-cat.com/api/calls/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..."
```

#### Success Response (200)
```json
{
  "callId": "550e8400-e29b-41d4-a716-446655440000",
  "calleeName": "Jane Doe",
  "phoneNumber": "+15555551234",
  "callerNumber": "+15551234567",
  "subject": "Appointment Confirmation",
  "prompt": "Please confirm your appointment for tomorrow at 3 PM",
  "status": "COMPLETED",
  "scheduledFor": null,
  "providerId": "retell_call_abc123",
  "aiLanguage": "en",
  "voiceId": "voice_id_123",
  "createdAt": 1703980700000,
  "updatedAt": 1703981000000,
  "completedAt": 1703981000000,
  "dialSuccessful": true,
  "callAnalyzed": true
}
```

#### Error Response (400)
```json
{
  "message": "Call not found",
  "success": false
}
```

---

### 16. Update Call

**Endpoint**: `PUT /api/calls/{callId}`  
**Description**: Update a call's information (only SCHEDULED calls can be updated)  
**Authentication**: Bearer token required

#### Path Parameters
- `callId`: UUID of the call

#### Request Body (At least one field required)
```json
{
  "calleeName": "Jane Smith",
  "phoneNumber": "+15555559999",
  "subject": "Updated Subject",
  "prompt": "Updated prompt text",
  "scheduledFor": 1703982400000,
  "aiLanguage": "en",
  "voiceId": "new_voice_id"
}
```

#### Validation Rules
- At least one field must be provided
- Same validation rules as create call
- Only SCHEDULED calls can be updated

#### Example cURL
```bash
curl -X PUT https://api.call-cat.com/api/calls/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..." \
  -H "Content-Type: application/json" \
  -d '{
    "calleeName": "Jane Smith-Johnson",
    "subject": "Rescheduled Appointment Confirmation"
  }'
```

#### Success Response (200)
```json
{
  "callId": "550e8400-e29b-41d4-a716-446655440000",
  "calleeName": "Jane Smith-Johnson",
  "phoneNumber": "+15555551234",
  "callerNumber": "+15551234567",
  "subject": "Rescheduled Appointment Confirmation",
  "prompt": "Please confirm your appointment for tomorrow at 3 PM",
  "status": "SCHEDULED",
  "scheduledFor": null,
  "providerId": null,
  "aiLanguage": "en",
  "voiceId": "voice_id_123",
  "createdAt": 1703980700000,
  "updatedAt": 1703981100000,
  "completedAt": null,
  "dialSuccessful": null,
  "callAnalyzed": false
}
```

#### Error Response (400)
```json
{
  "message": "Cannot update completed calls",
  "success": false
}
```

---

### 17. Delete Call

**Endpoint**: `DELETE /api/calls/{callId}`  
**Description**: Delete a call (only SCHEDULED calls can be deleted)  
**Authentication**: Bearer token required

#### Path Parameters
- `callId`: UUID of the call

#### Example cURL
```bash
curl -X DELETE https://api.call-cat.com/api/calls/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..."
```

#### Success Response (200)
```json
{
  "message": "Call deleted successfully",
  "success": true
}
```

#### Error Response (400)
```json
{
  "message": "Cannot delete completed calls",
  "success": false
}
```

---

### 18. Trigger Call (Lambda/Internal Only)

**Endpoint**: `POST /api/calls/{callId}/trigger`  
**Description**: Internal endpoint used by AWS Lambda to trigger scheduled calls  
**Authentication**: API Key required (X-API-Key header)

#### Headers
- `X-API-Key`: Internal API key for Lambda authentication

#### Path Parameters
- `callId`: UUID of the call to trigger

#### Example cURL (Internal Use Only)
```bash
curl -X POST https://api.call-cat.com/api/calls/550e8400-e29b-41d4-a716-446655440000/trigger \
  -H "X-API-Key: 576cfcaa-3f0b-4024-abb4-f371afb979d7"
```

#### Success Response (200)
```json
{
  "message": "Call triggered successfully",
  "success": true
}
```

#### Error Response (401)
```json
{
  "message": "Unauthorized",
  "success": false
}
```

---

## Transcript Endpoints (Protected)

### 19. Get Live Transcript

**Endpoint**: `GET /api/live_transcripts/{providerId}`  
**Description**: Get real-time transcript for an active call by provider ID  
**Authentication**: Bearer token required

#### Path Parameters
- `providerId`: Retell AI provider call ID

#### Example cURL
```bash
curl -X GET https://api.call-cat.com/api/live_transcripts/retell_call_abc123 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NDUyMjQ4MCwiZXhwIjoxNjk0NjA4ODgwfQ.abc123..."
```

#### Success Response (200)
```json
{
  "providerId": "retell_call_abc123",
  "transcriptText": "AI: Hello, this is a reminder about your appointment tomorrow at 3 PM. Can you please confirm if you'll be able to attend?\n\nUser: Yes, I'll be there.\n\nAI: Perfect! Thank you for confirming. We look forward to seeing you tomorrow at 3 PM."
}
```

#### Error Response (400)
```json
{
  "message": "Transcript not found",
  "success": false
}
```

---

## Webhook Endpoints (Public)

### 20. Retell Webhook

**Endpoint**: `POST /webhooks/retell`  
**Description**: Webhook endpoint for Retell AI to notify about call events  
**Authentication**: None (webhook verification handled internally)

#### Event Types
- `call_started`: Call has started
- `call_ended`: Call has ended  
- `call_analyzed`: Call analysis is complete

#### Example Webhook Payload (call_started)
```json
{
  "event": "call_started",
  "call": {
    "call_id": "retell_call_abc123",
    "start_timestamp": 1703981000,
    "phone_number": "+15555551234"
  }
}
```

#### Response (204)
No content - webhook always returns 204 to avoid retries

---

## Health Check Endpoint (Public)

### 21. Health Check

**Endpoint**: `GET /health`  
**Description**: Basic health check endpoint  
**Authentication**: None required

#### Example cURL
```bash
curl -X GET https://api.call-cat.com/health
```

#### Success Response (200)
```
OK
```

---

## Error Response Format

All endpoints follow a consistent error response format:

#### General Error Response
```json
{
  "message": "Descriptive error message",
  "success": false
}
```

#### Common HTTP Status Codes
- `200`: Success
- `400`: Bad Request (validation errors, business logic errors)
- `401`: Unauthorized (invalid/missing JWT token)
- `404`: Not Found (resource doesn't exist)
- `500`: Internal Server Error

---

## Data Formats and Validation

### Phone Numbers
- Must be in E.164 format: `+1XXXXXXXXXX` (US numbers only)
- Example: `+15551234567`

### Timestamps  
- Unix timestamp in milliseconds
- Example: `1703980800000` (represents 2023-12-30 10:00:00 UTC)

### JWT Token Expiration
- Tokens expire after 24 hours (86400000 milliseconds)
- Refresh by logging in again

### UUIDs
- Call IDs are UUIDs: `550e8400-e29b-41d4-a716-446655440000`
- Non-enumerable for security

### String Limits
- `calleeName`: 100 characters max
- `subject`: 200 characters max  
- `prompt`: 5000 characters max
- `systemPrompt`: 1000 characters max
- `firstName`/`lastName`: 50 characters max
- `aiLanguage`: 10 characters max
- `voiceId`: 100 characters max

---

## Rate Limiting and Usage

- No explicit rate limiting currently implemented
- JWT tokens provide natural rate limiting (24-hour expiration)
- Call creation may have provider-specific limits via Retell AI

---

## Examples

### Complete Registration Flow
```bash
# 1. Send verification email
curl -X POST https://api.call-cat.com/api/auth/send-verification \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'

# 2. Verify email with code
curl -X POST https://api.call-cat.com/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "code": "123456"}'

# 3. Register account
curl -X POST https://api.call-cat.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123",
    "firstName": "John", 
    "lastName": "Doe"
  }'
```

### Complete Call Management Flow
```bash
# 1. Login and get token
TOKEN=$(curl -X POST https://api.call-cat.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "SecurePass123"}' \
  | jq -r '.token')

# 2. Create immediate call
curl -X POST https://api.call-cat.com/api/calls \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "calleeName": "Jane Doe",
    "phoneNumber": "+15555551234", 
    "subject": "Appointment Reminder",
    "prompt": "This is a reminder about your appointment"
  }'

# 3. List scheduled calls
curl -X GET "https://api.call-cat.com/api/calls?status=SCHEDULED" \
  -H "Authorization: Bearer $TOKEN"

# 4. Get specific call details
curl -X GET https://api.call-cat.com/api/calls/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer $TOKEN"
```

This documentation provides complete coverage of all available API endpoints with real examples based on the actual backend implementation.