## Call Management Requirements Brainstorm (MVP → v1)

### Goals
- Provide endpoints to create, edit, list, and delete/cancel calls.
- Support scheduled calls (future), active/in-progress calls, and completed calls.
- Capture post-call artifacts: transcript, summary, action items, tags, sentiment.

### Scope for MVP
- Calls Controller: add, edit, delete, list, get details.
- Transcript append/list for a call.
- Simple post-call finalize operation to compute/store summary and action items (implementation can be stubbed initially).

### Glossary
- "Call" = a unit representing a planned or executed interaction (phone/VoIP/WebRTC).
- "Scheduled" = planned with `scheduledAtEpoch` in the future.
- "Completed" = ended with `endAtEpoch` and status `COMPLETED` (or final states like `NO_SHOW`, `CANCELED`).

---

### Data Model (DynamoDB)
## Data Model (DynamoDB)
Use DynamoDB for call lifecycle and separate table for transcripts (high-write, timeline reads). PostgreSQL remains for users and preferences.

- Table: `callcat-calls` (from `aws.dynamodb.table.calls`)
  - Keys
    - Partition key: `userId` (Long)
    - Sort key: `callId` (String, UUID)
  - Core fields
    - `calleeName` (String)
    - `phoneNumber` (String)
    - `subject` (String) quick description
    - `prompt` (String) detailed task description for voice agent
    - `status` (String enum: `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`)
    - `scheduledAt` (Long epoch)
    - `callAt` (Long epoch)
    - `aiLanguage` (String)
    - `voiceId` (String)
    - `createdAt`(Long epoch)
    - `updatedAt`(Long epoch)

  - Fields after completion
    - `completedAt` (Long epoch, set when status becomes COMPLETED)
    - `summary` (String, optional)
    - `durationSec` (Integer, optional)
    - `outcome` (String enum: `SUCCESSFUL`, `NEEDS_ATTENTION`, `FAILED`)
    - `transcriptUrl` (String, optional) # Link to transcript table
    - `audioRecordingUrl` (String, optional) # S3 link

  - Global Secondary Indexes
    - GSI 1: `upcoming-calls-index`:
      - PK: `userId` (Long)
      - SK: `scheduledAt` (Long epoch)
      - Purpose: Query upcoming SCHEDULED/IN_PROGRESS calls, sorted chronologically
    - GSI 2: `completed-calls-index`:
      - PK: `userId` (Long) 
      - SK: `completedAt` (Long epoch)
      - Purpose: Query recent COMPLETED calls
      - Note: Query with ScanIndexForward=false for most recent first

- Table: `callcat-transcripts` (separate table for transcript data)
  - Keys
    - Partition key: `callId` (String)
  - Fields
    - `transcriptText` (String)


Notes
- Prefer epoch Longs for sort/range queries.
- Keep fields small where possible; transcripts store the heavy text.

---

### Calls Controller (MVP API)
All endpoints are user-scoped (authenticated user can only read/write own calls).

**POST /api/calls** - Create new call
- Request: `{ calleeName, phoneNumber, subject, prompt, scheduledAt?, aiLanguage?, voiceId? }`
- Response: `CallResponse` with created call details
- Auto-generates: callId (UUID), createdAt, updatedAt, status=SCHEDULED

**GET /api/calls** - List user's calls
- Query: `status?` (filter), `limit?` (default 20, max 100)
- Response: `{ calls: [CallResponse], nextToken? }`
- Uses appropriate GSI (upcoming-calls-index or completed-calls-index)

**GET /api/calls/{callId}** - Get call details
- Response: `CallResponse`

**PUT /api/calls/{callId}** - Update call
- Request: `{ calleeName?, phoneNumber?, subject?, prompt?, scheduledAt?, status? }`
- Response: `CallResponse`
- Auto-updates: updatedAt

**DELETE /api/calls/{callId}** - Delete call
- Only for SCHEDULED status
- Response: `204 No Content`

### Transcript Controller (MVP API)

**GET /api/calls/{callId}/transcript** - Get transcript
- Response: `{ callId, transcriptText }`

**PUT /api/calls/{callId}/transcript** - Save transcript
- Request: `{ transcriptText }`
- Response: `{ callId, transcriptText }`
### Validation & Rules

**Required Fields:**
- `calleeName` (1-100 chars)
- `phoneNumber` (valid US format)
- `subject` (1-200 chars) 
- `prompt` (1-5000 chars)

**Business Rules:**
- scheduledAt must be future time
- Status transitions: SCHEDULED → IN_PROGRESS → COMPLETED
- Only SCHEDULED calls can be deleted
- Phone number validation (US E.164 format)

---

### Authorization & Security

- JWT authentication required
- User can only access own calls (userId scoping)
- Call ownership verified on all operations
- Phone numbers encrypted at rest

---

### Pagination & Sorting

- DynamoDB pagination with nextToken
- SCHEDULED: sorted by scheduledAt (soonest first)
- COMPLETED: sorted by completedAt (recent first via reverse scan)
- Uses separate GSIs for efficiency
---

### Error Model (examples)
- 400: validation error (e.g., invalid transition)
- 401: unauthenticated
- 403: forbidden (call does not belong to user)
- 404: not found (callId)
- 409: conflict (cancel after start, overlapping reschedule if we enforce constraints)

---

### DTO Sketches

**CreateCallRequest:**
```json
{
  "calleeName": "John Doe",
  "phoneNumber": "+15551234567", 
  "subject": "Project follow-up",
  "prompt": "Call John to discuss project timeline and next steps. Be professional and take notes on any concerns.",
  "scheduledAt": 1672531200000
}
```

**CallResponse:**
```json
{
  "callId": "abc-123",
  "calleeName": "John Doe",
  "subject": "Project follow-up", 
  "prompt": "Call John to discuss...",
  "status": "SCHEDULED",
  "scheduledAt": 1672531200000,
  "createdAt": 1672444800000
}
```


### UI Mapping to Mock
ADD HERE
---


---

### Open Questions


---

### Implementation Plan

**✅ Phase 1: Data Model**
- Updated CallRecord with prompt field
- Updated CallTranscript (simplified)
- Added GSI for efficient queries

**🔄 Phase 2: Controllers & DTOs**
- Create simple request/response DTOs
- Implement CallController (5 endpoints)
- Implement TranscriptController (2 endpoints)

**⏳ Phase 3: Services & Validation**  
- CallService with business logic
- Phone number validation
- JWT user context extraction

