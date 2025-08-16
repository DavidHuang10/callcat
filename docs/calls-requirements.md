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

Table: callcat-calls
Partition Key: userId (Number)  # Associates calls with users
Sort Key: callId (String)       # callcat-ID (cc-0001, 0002, etc.)

# Pre-call fields (always present)
Attributes:
- calleeName: String           # Who we're calling
- phoneNumber: String          # E.164 format
- callerNumber: String
- subject: String              # Quick description
- prompt: String               # Detailed instructions for AI
- status: String               # SCHEDULED|IN_PROGRESS|COMPLETED|FAILED
- scheduledFor: Number          # Epoch time when call should happen
- aiLanguage: String           # Language for AI to speak
- voiceId: String              # Retell voice selection
- createdAt: Number            # Epoch time when created
- updatedAt: Number            # Epoch time last modified

    # During-call fields (populated when call starts)
    - providerId: String        # Retell's call ID
    - callStartedAt: Number               # Epoch time when call actually started
    # Post-call fields (optional, populated after completion)
    - completedAt: Number          # Epoch time when ended
    # Retell-specific data storage
    - retellCallData: String          # Full Retell webhook response

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
    - Partition key: `callId` (String) (THIS IS THE RETELL CALL ID)
  - Fields
    - `transcriptText` (String)
 - TTL: 90 days. use ExpiresAt, its already set up.

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

## IMPORTANT: RETELL RESPONSE FORMAT AFTER COMPLETED CALL
# retell will send portions of this response depending on what stage the phone call is in

{
"call_type": "phone_call",
"from_number": "+12137771234",
"to_number": "+12137771235",
"direction": "inbound",
"telephony_identifier": {
"twilio_call_sid": "CA5d0d0d8047bf685c3f0ff980fe62c123"
},
"call_id": "Jabr9TXYYJHfvl6Syypi88rdAHYHmcq6",
"agent_id": "oBeDLoLOeuAbiuaMFXRtDOLriTJ5tSxD",
"agent_version": 1,
"call_status": "registered",
"metadata": {},
"retell_llm_dynamic_variables": {
"customer_name": "John Doe"
},
"collected_dynamic_variables": {
"last_node_name": "Test node"
},
"custom_sip_headers": {
"X-Custom-Header": "Custom Value"
},
"opt_out_sensitive_data_storage": true,
"opt_in_signed_url": true,
"start_timestamp": 1703302407333,
"end_timestamp": 1703302428855,
"duration_ms": 10000,
"transcript": "Agent: hi how are you doing?\nUser: Doing pretty well. How are you?\nAgent: That's great to hear! I'm doing well too, thanks! What's up?\nUser: I don't have anything in particular.\nAgent: Got it, just checking in!\nUser: Alright. See you.\nAgent: have a nice day",
"transcript_object": [
{
"role": "agent",
"content": "hi how are you doing?",
"words": [
{
"word": "hi",
"start": 0.7,
"end": 1.3
}
]
}
],
"transcript_with_tool_calls": [
{
"role": "agent",
"content": "hi how are you doing?",
"words": [
{
"word": "hi",
"start": 0.7,
"end": 1.3
}
]
}
],
"recording_url": "https://retellai.s3.us-west-2.amazonaws.com/Jabr9TXYYJHfvl6Syypi88rdAHYHmcq6/recording.wav",
"public_log_url": "https://retellai.s3.us-west-2.amazonaws.com/Jabr9TXYYJHfvl6Syypi88rdAHYHmcq6/public_log.txt",
"knowledge_base_retrieved_contents_url": "https://retellai.s3.us-west-2.amazonaws.com/Jabr9TXYYJHfvl6Syypi88rdAHYHmcq6/kb_retrieved_contents.txt",
"latency": {
"e2e": {
"p50": 800,
"p90": 1200,
"p95": 1500,
"p99": 2500,
"max": 2700,
"min": 500,
"num": 10,
"values": [
123
]
},
"llm": {
"p50": 800,
"p90": 1200,
"p95": 1500,
"p99": 2500,
"max": 2700,
"min": 500,
"num": 10,
"values": [
123
]
},
"llm_websocket_network_rtt": {
"p50": 800,
"p90": 1200,
"p95": 1500,
"p99": 2500,
"max": 2700,
"min": 500,
"num": 10,
"values": [
123
]
},
"tts": {
"p50": 800,
"p90": 1200,
"p95": 1500,
"p99": 2500,
"max": 2700,
"min": 500,
"num": 10,
"values": [
123
]
},
"knowledge_base": {
"p50": 800,
"p90": 1200,
"p95": 1500,
"p99": 2500,
"max": 2700,
"min": 500,
"num": 10,
"values": [
123
]
},
"s2s": {
"p50": 800,
"p90": 1200,
"p95": 1500,
"p99": 2500,
"max": 2700,
"min": 500,
"num": 10,
"values": [
123
]
}
},
"disconnection_reason": "agent_hangup",
"call_analysis": {
"call_summary": "The agent called the user to ask question about his purchase inquiry. The agent asked several questions regarding his preference and asked if user would like to book an appointment. The user happily agreed and scheduled an appointment next Monday 10am.",
"in_voicemail": false,
"user_sentiment": "Positive",
"call_successful": true,
"custom_analysis_data": {}
},
"call_cost": {
"product_costs": [
{
"product": "elevenlabs_tts",
"unit_price": 1,
"cost": 60
}
],
"total_duration_seconds": 60,
"total_duration_unit_price": 1,
"combined_cost": 70
},
"llm_token_usage": {
"values": [
123
],
"average": 123,
"num_requests": 123
}
}