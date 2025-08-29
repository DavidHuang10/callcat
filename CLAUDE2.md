# CallCat Backend API - Frontend Developer Guide

## 🌐 Production API Base URL
```
https://callcat-backend.us-east-1.elasticbeanstalk.com
```

## 🔑 Authentication Flow

### 1. Email Verification (Required First)
```javascript
// Send verification code to email
POST /api/auth/send-verification
{
  "email": "user@example.com"
}

// Verify the email with code
POST /api/auth/verify-email
{
  "email": "user@example.com", 
  "code": "123456"
}
```

### 2. User Registration
```javascript
POST /api/auth/register
{
  "email": "user@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}

// Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "uuid-here",
  "email": "user@example.com", 
  "fullName": "John Doe",
  "expirationTime": 86400000
}
```

### 3. Login
```javascript
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "SecurePass123"
}

// Same response format as registration
```

### 4. Logout
```javascript
POST /api/auth/logout
Headers: {
  "Authorization": "Bearer your-jwt-token"
}
```

## 📱 API Endpoints Reference

### Authentication (Public)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user  
- `POST /api/auth/logout` - Logout user
- `POST /api/auth/send-verification` - Send email verification
- `POST /api/auth/verify-email` - Verify email code
- `POST /api/auth/forgot-password` - Send password reset
- `POST /api/auth/reset-password` - Reset password

### User Management (Protected)
- `GET /api/user/profile` - Get user profile
- `PUT /api/user/profile` - Update profile  
- `POST /api/user/change-password` - Change password
- `GET /api/user/preferences` - Get user preferences
- `PUT /api/user/preferences` - Update preferences

### Call Management (Protected)
- `POST /api/calls` - Create/schedule call
- `GET /api/calls?status=SCHEDULED&limit=20` - List calls
- `GET /api/calls/{callId}` - Get call details
- `PUT /api/calls/{callId}` - Update call
- `DELETE /api/calls/{callId}` - Delete call
- `GET /api/calls/{callId}/transcript` - Get transcript

## 🔐 Authentication Headers

All protected endpoints require:
```javascript
Headers: {
  "Authorization": "Bearer your-jwt-token",
  "Content-Type": "application/json"
}
```

## 📞 Call Creation Examples

### Schedule Immediate Call
```javascript
POST /api/calls
Headers: { "Authorization": "Bearer token" }
{
  "phoneNumber": "+15551234567",
  "aiPrompt": "You are a friendly customer service agent.",
  "aiLanguage": "en",
  "maxCallDuration": 300
}
```

### Schedule Future Call  
```javascript
POST /api/calls
Headers: { "Authorization": "Bearer token" }
{
  "phoneNumber": "+15551234567", 
  "aiPrompt": "Remind about appointment tomorrow",
  "scheduledFor": 1703980800000, // Unix timestamp in milliseconds
  "aiLanguage": "en"
}
```

## 📊 Call Response Format
```javascript
{
  "callId": "uuid-here",
  "phoneNumber": "+15551234567",
  "status": "SCHEDULED", // or "COMPLETED"
  "aiPrompt": "Your prompt here",
  "aiLanguage": "en",
  "scheduledFor": 1703980800000,
  "createdAt": 1703980700000,
  "updatedAt": 1703980700000,
  "completedAt": null, // Set when call completes
  "dialSuccessful": null, // true/false when call completes
  "providerId": null // Retell AI call ID when call starts
}
```

## 👤 User Profile Format
```javascript
{
  "id": "user-uuid",
  "email": "user@example.com", 
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "isActive": true
}
```

## ⚙️ User Preferences Format
```javascript
{
  "timezone": "UTC",
  "emailNotifications": true,
  "voiceId": "voice_id_123", 
  "systemPrompt": "Custom AI instructions"
}
```

## 📋 Call Listing
```javascript
GET /api/calls?status=SCHEDULED&limit=10
Headers: { "Authorization": "Bearer token" }

// Response:
{
  "calls": [
    // Array of call objects
  ]
}
```

**Required Parameters:**
- `status`: "SCHEDULED" or "COMPLETED" (required)
- `limit`: Max 100 (optional, defaults to 20)

## 🔄 Real-time Features

### Call Status Updates
Calls automatically transition from `SCHEDULED` → `COMPLETED` via webhooks.

### Live Transcripts  
- Available at `GET /api/calls/{callId}/transcript`
- Updates in real-time during active calls
- Polls every 3 seconds during call

## ❌ Error Handling

### Error Response Format
```javascript
{
  "message": "Error description", 
  "success": false
}
```

### Common HTTP Status Codes
- `200` - Success
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (invalid/missing token)
- `404` - Not Found (resource doesn't exist)

### Common Validation Errors
- Phone numbers must be E.164 format (`+1XXXXXXXXXX`)
- Email must be verified before registration
- Passwords require 8+ chars with uppercase, lowercase, number
- JWT tokens expire after 24 hours

## 🌍 CORS Configuration
The API accepts requests from:
- `http://localhost:*` (development)
- `https://call-cat.com` (production) 
- `https://*.call-cat.com` (subdomains)
- `https://*.ngrok.io` (testing)

## 📱 Frontend Integration Tips

### JWT Token Management
```javascript
// Store token after login/registration
localStorage.setItem('callcat_token', response.token);

// Add to all API requests  
const token = localStorage.getItem('callcat_token');
fetch('/api/calls', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

// Clear on logout
localStorage.removeItem('callcat_token');
```

### Phone Number Validation
```javascript
function validatePhoneNumber(phone) {
  const pattern = /^\+1[0-9]{10}$/;
  return pattern.test(phone);
}
```

### Date/Time Handling
```javascript
// Convert Date to Unix timestamp (milliseconds)
const scheduledFor = new Date('2024-01-01 10:00:00').getTime();

// Convert timestamp back to Date
const callDate = new Date(response.scheduledFor);
```

### Call Status Polling
```javascript
async function pollCallStatus(callId) {
  const response = await fetch(`/api/calls/${callId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const call = await response.json();
  
  if (call.status === 'COMPLETED') {
    // Call finished, get transcript
    const transcript = await fetch(`/api/calls/${callId}/transcript`);
  }
}
```

## 🚀 Production Deployment Info

- **Platform**: AWS Elastic Beanstalk
- **Instance**: t3.micro (FREE tier eligible)
- **Scaling**: Auto-scaling enabled
- **SSL**: HTTPS enabled by default
- **Health Monitoring**: Built-in AWS health checks

## 🔧 Environment

The backend is fully deployed and operational in production with:
- PostgreSQL RDS for user data
- DynamoDB for call records and transcripts  
- AWS Lambda for scheduled call execution
- Real-time webhook processing
- Multi-threaded live transcript polling

**Ready for frontend development!** 🎉