# CallCat Call Management System - Complete Architecture Guide

## Overview
CallCat's call management system allows users to schedule, execute, and track AI-powered phone calls. The system integrates with Retell AI for actual call execution and uses a hybrid database approach for optimal performance.

---

## 🗄️ Data Storage Architecture

### Database Strategy: Hybrid PostgreSQL + DynamoDB

**PostgreSQL (Relational Data)**
- **Purpose**: User accounts, authentication, preferences
- **Tables**: 
  - `users` - User accounts and authentication data
  - `user_preferences` - User settings (timezone, notifications, voice, system prompt)
  - `email_verifications` - Email verification codes and status

**DynamoDB (NoSQL - High Performance)**
- **Purpose**: Call lifecycle data requiring fast reads/writes and time-based queries
- **Tables**:
  - `callcat-calls` - Call metadata and lifecycle tracking
  - `callcat-transcripts` - Call transcripts (separate for performance)
  - `callcat-blacklist` - JWT token blacklist with TTL

### DynamoDB Table Design: `callcat-calls`

**Primary Key Structure:**
```
Partition Key: userId (Long)     # User who owns the call
Sort Key: callId (String)        # Our internal UUID (e.g., "abc-123-def-456")
```

**Global Secondary Indexes (GSI):**
```
GSI 1: upcoming-calls-index
  - PK: userId (Long)
  - SK: scheduledAt (Long epoch)
  - Purpose: Query upcoming SCHEDULED/IN_PROGRESS calls chronologically

GSI 2: completed-calls-index  
  - PK: userId (Long)
  - SK: completedAt (Long epoch)
  - Purpose: Query recent COMPLETED calls (reverse scan for most recent first)
```

**Call Record Fields:**
```java
// Pre-call fields (always present)
- userId: Long              # Owner of the call
- callId: String            # Our internal UUID 
- calleeName: String        # Who we're calling
- phoneNumber: String       # E.164 format phone number
- callerNumber: String      # Our outbound caller ID
- subject: String           # Quick description of call purpose
- prompt: String            # Detailed AI instructions
- status: String            # SCHEDULED|IN_PROGRESS|COMPLETED|FAILED|CANCELED
- scheduledAt: Long         # Epoch time when call should happen
- aiLanguage: String        # Language for AI (default: "en")
- voiceId: String           # Retell voice selection
- createdAt: Long           # Epoch time when created
- updatedAt: Long           # Epoch time last modified

// During-call fields (populated when call starts)
- providerId: String        # Retell's call ID (e.g., "Jabr9TXYYJHfvl6Syypi88rdAHYHmcq6")
- callAt: Long              # Epoch time when call actually started

// Post-call fields (populated after completion)
- completedAt: Long         # Epoch time when call ended
- durationSec: Integer      # Call duration in seconds
- summary: String           # AI-generated call summary
- outcome: String           # SUCCESS|FAILED|NO_ANSWER|BUSY
- audioRecordingUrl: String # URL to call recording
- transcriptUrl: String     # URL to transcript (if stored externally)
- retellCallData: String    # Complete JSON response from Retell (for analysis)
```

---

## 📊 DTO (Data Transfer Object) Architecture

### Request DTOs (Client → Server)

**CreateCallRequest.java**
```java
{
  "calleeName": "John Doe",
  "phoneNumber": "+15551234567", 
  "subject": "Project follow-up",
  "prompt": "Call John to discuss project timeline...",
  "scheduledAt": 1672531200000,    // Optional: epoch timestamp
  "aiLanguage": "en",              // Optional: defaults to "en"
  "voiceId": "voice_id_123"        // Optional: user's preferred voice
}
```

**UpdateCallRequest.java**
```java
{
  "calleeName": "John Smith",       // Optional: update name
  "phoneNumber": "+15559876543",    // Optional: update number
  "subject": "Updated subject",     // Optional: update subject
  "prompt": "New instructions...",  // Optional: update AI prompt
  "scheduledAt": 1672617600000,     // Optional: reschedule
  "status": "CANCELED",             // Optional: change status
  "aiLanguage": "es",              // Optional: change language
  "voiceId": "new_voice_id"        // Optional: change voice
}
```

### Response DTOs (Server → Client)

**CallResponse.java**
```java
{
  "callId": "abc-123-def-456",
  "calleeName": "John Doe",
  "phoneNumber": "+15551234567",
  "callerNumber": "+15559999999",
  "subject": "Project follow-up",
  "prompt": "Call John to discuss...",
  "status": "COMPLETED",
  "scheduledAt": 1672531200000,
  "callAt": 1672531245000,         // When call actually started
  "completedAt": 1672531545000,    // When call ended
  "providerId": "Jabr9TXY...",     // Retell's call ID
  "aiLanguage": "en",
  "voiceId": "voice_id_123",
  "createdAt": 1672444800000,
  "updatedAt": 1672531545000,
  "summary": "Discussed project timeline...",
  "durationSec": 300,
  "outcome": "SUCCESS",
  "transcriptUrl": "https://...",
  "audioRecordingUrl": "https://..."
}
```

**CallListResponse.java**
```java
{
  "calls": [CallResponse, CallResponse, ...],
  "nextToken": "pagination_token_here"    // For pagination (currently null)
}
```

---

## 🔄 Data Flow & API Endpoints

### 1. Create Call Flow
```
POST /api/calls
├── Authentication: JWT token → extract user email
├── Validation: Phone number format, future scheduling time
├── Business Logic: 
│   ├── Generate UUID for callId
│   ├── Set status = "SCHEDULED"
│   ├── Apply defaults (aiLanguage="en", scheduledAt=now if not provided)
│   └── Save to DynamoDB
└── Response: CallResponse DTO
```

**DTO Transformation:**
```
CreateCallRequest → CallRecord → CallResponse
      ↓               ↓            ↑
   Validation    Save to DB    Map fields
```

### 2. List Calls Flow
```
GET /api/calls?status=SCHEDULED&limit=20
├── Authentication: JWT token → extract user email
├── Query Strategy:
│   ├── If status provided → filter by status using appropriate GSI
│   ├── If no status → get mix of upcoming + completed calls
│   └── Use GSI indexes for efficient time-based queries
└── Response: CallListResponse with array of CallResponse DTOs
```

**Query Patterns:**
```java
// SCHEDULED/IN_PROGRESS calls
upcomingCallsIndex.query(userId, scheduledAt_range)

// COMPLETED calls  
completedCallsIndex.query(userId, reverse_scan_by_completedAt)

// Mixed results
List<CallRecord> upcoming = queryUpcoming(userId, limit/2);
List<CallRecord> completed = queryCompleted(userId, limit/2);
```

### 3. Update Call Flow
```
PUT /api/calls/{callId}
├── Authentication: JWT token → extract user email
├── Authorization: Verify call belongs to user
├── Validation: 
│   ├── Phone number format (if provided)
│   ├── Status transitions (SCHEDULED→IN_PROGRESS→COMPLETED)
│   └── Future scheduling time (if rescheduling)
├── Business Logic: Update only provided fields
└── Response: Updated CallResponse DTO
```

**Status Transition Rules:**
```
SCHEDULED → IN_PROGRESS ✓
SCHEDULED → CANCELED ✓
IN_PROGRESS → COMPLETED ✓
IN_PROGRESS → FAILED ✓
COMPLETED/FAILED/CANCELED → * ✗ (immutable)
```

### 4. Delete Call Flow
```
DELETE /api/calls/{callId}
├── Authentication: JWT token → extract user email
├── Authorization: Verify call belongs to user
├── Business Rule: Only SCHEDULED calls can be deleted
├── Action: Remove from DynamoDB
└── Response: Success message
```

---

## 🔍 Data Retrieval Patterns

### Repository Layer (CallRecordRepository.java)

**Efficient Key-Based Lookups:**
```java
// O(1) lookup - uses primary key
findByUserIdAndCallId(userId, callId) 
```

**Time-Based Queries (Using GSI):**
```java
// Query upcoming calls sorted by scheduled time
findUpcomingCallsByUserId(userId, limit)
  → Uses upcoming-calls-index GSI
  → Query: PK=userId, SK=scheduledAt range
  → Returns chronologically ordered results

// Query completed calls sorted by completion time (recent first)
findCompletedCallsByUserId(userId, limit)
  → Uses completed-calls-index GSI  
  → Query: PK=userId, scanIndexForward=false
  → Returns reverse chronologically ordered results
```

**Filtered Queries:**
```java
// Status-based filtering using efficient GSI queries + in-memory filtering
findByUserIdAndStatus(userId, status, limit)
  → SCHEDULED/IN_PROGRESS: query upcoming index + filter
  → COMPLETED: query completed index (no filter needed)
```

**Inefficient Scans (Temporary):**
```java
// These require full table scans - should be replaced with GSI in production
findByCallId(callId)         // Used by webhooks - scans for callId
findByProviderId(providerId) // Removed - was inefficient
```

---

## 🧠 Business Logic Layer (CallService.java)

### Core Service Methods

**createCall(userEmail, CreateCallRequest)**
```java
1. Resolve user by email → get userId
2. Validate phone number (E.164 format)
3. Validate scheduling (must be future time)
4. Create CallRecord entity:
   ├── Generate UUID for callId
   ├── Set userId (from authenticated user)
   ├── Copy fields from request
   ├── Apply defaults (status="SCHEDULED", aiLanguage="en")
   └── Set timestamps (createdAt, updatedAt)
5. Save to DynamoDB
6. Transform to CallResponse DTO
```

**getCalls(userEmail, status, limit)**
```java
1. Resolve user by email → get userId
2. Query strategy based on status:
   ├── Specific status → filter using appropriate GSI
   └── No status → mixed query (upcoming + completed)
3. Transform List<CallRecord> → List<CallResponse>
4. Wrap in CallListResponse with pagination token
```

**updateCall(userEmail, callId, UpdateCallRequest)**
```java
1. Resolve user by email → get userId
2. Fetch existing call (authorization check)
3. Validate updates:
   ├── Phone number format
   ├── Status transitions
   └── Future scheduling
4. Apply selective updates (only non-null fields)
5. Update timestamps
6. Save to DynamoDB
7. Transform to CallResponse DTO
```

### Validation & Business Rules

**Phone Number Validation (PhoneNumberValidator.java)**
```java
validatePhoneNumber(phoneNumber)
├── Check E.164 format: starts with +, 10-15 digits
├── Currently US-focused validation
└── Throws IllegalArgumentException if invalid
```

**Status Transition Validation**
```java
validateStatusTransition(currentStatus, newStatus)
├── SCHEDULED → {IN_PROGRESS, CANCELED} ✓
├── IN_PROGRESS → {COMPLETED, FAILED} ✓  
├── {COMPLETED, FAILED, CANCELED} → * ✗
└── Throws IllegalArgumentException for invalid transitions
```

**Authorization Pattern**
```java
// Every operation verifies call ownership
User user = userRepository.findByEmailAndIsActive(email, true);
CallRecord call = callRecordRepository.findByUserIdAndCallId(user.getId(), callId);
// Throws RuntimeException if user not found or call doesn't belong to user
```

---

## 🎯 Entity Mapping Strategy

### Current Approach (Manual Mapping)
```java
private CallResponse mapToCallResponse(CallRecord callRecord) {
    CallResponse response = new CallResponse();
    response.setCallId(callRecord.getCallId());
    response.setCalleeName(callRecord.getCalleeName());
    // ... 20+ field assignments
    return response;
}
```

**Pros:** Simple, explicit, no dependencies
**Cons:** Verbose, error-prone, hard to maintain

### Enterprise Alternatives
```java
// Option 1: MapStruct (Recommended)
@Mapper(componentModel = "spring")
public interface CallMapper {
    CallResponse toResponse(CallRecord callRecord);
    CallRecord toEntity(CreateCallRequest request);
}

// Option 2: ModelMapper
private CallResponse mapToCallResponse(CallRecord callRecord) {
    return modelMapper.map(callRecord, CallResponse.class);
}

// Option 3: Builder Pattern
private CallResponse mapToCallResponse(CallRecord callRecord) {
    return CallResponse.builder()
        .callId(callRecord.getCallId())
        .calleeName(callRecord.getCalleeName())
        .build();
}
```

---

## 🔐 Security & Authorization

### Authentication Flow
```
Client Request → JWT Filter → Extract email → Service methods use email for user resolution
```

### Authorization Pattern
```java
// Every service method follows this pattern:
1. Extract user email from Authentication
2. Resolve User entity from database  
3. Use userId for all database operations
4. Implicit authorization: users can only access their own calls
```

### Data Protection
- Phone numbers stored in E.164 format
- JWT tokens blacklisted on logout (DynamoDB with TTL)
- No sensitive data in logs
- User-scoped data access (userId filtering)

---

## 📈 Performance Considerations

### Efficient Query Patterns
```java
// ✅ GOOD: Uses primary key
findByUserIdAndCallId(userId, callId)

// ✅ GOOD: Uses GSI with time-based sorting  
findUpcomingCallsByUserId(userId, limit)

// ❌ BAD: Full table scan
findByCallId(callId)  // Currently used by webhooks - needs improvement
```

### Pagination Strategy
- DynamoDB native pagination with `nextToken`
- Currently not fully implemented (returns null)
- Designed for future enhancement

### Indexing Strategy
- Primary key: `userId + callId` for ownership-based access
- GSI 1: `userId + scheduledAt` for upcoming calls
- GSI 2: `userId + completedAt` for call history
- Missing: GSI on `callId` for webhook efficiency (future enhancement)

---

## 🔧 Current Limitations & Future Enhancements

### Known Issues
1. **Webhook Efficiency**: `findByCallId()` requires table scan
2. **Pagination**: Not fully implemented in responses
3. **Mapping Verbosity**: Manual field mapping is error-prone
4. **Retell Integration**: Not yet connected to actual API calls

### Recommended Improvements
1. Add GSI on `callId` for O(1) webhook lookups
2. Implement MapStruct for clean entity mapping
3. Add comprehensive pagination with `nextToken` support
4. Integrate actual Retell API for call execution
5. Add call scheduling/trigger system
6. Implement real-time status updates via WebSocket

---

## 🎯 Summary

The CallCat call management system uses a **hybrid database approach** with **PostgreSQL for users** and **DynamoDB for call data**. The architecture emphasizes **performance through strategic indexing**, **security through user-scoped access**, and **maintainability through clear separation of concerns**.

The system handles the complete call lifecycle from creation to completion, with **efficient time-based queries** for call listing and **robust validation** for business rules. While the current implementation is functional and secure, there are clear paths for optimization, particularly around webhook handling and entity mapping.

The DTO flow is clean and follows REST conventions, with proper validation and error handling throughout the request/response cycle.