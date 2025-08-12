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
Use DynamoDB for call lifecycle and transcript (high-write, timeline reads). PostgreSQL remains for users and preferences.

- Table: `callcat-calls` (from `aws.dynamodb.table.calls`)
  - Keys
    - Partition key: `userId` (Long)
    - Sort key: `callId` (String, UUID)
  - Core fields
    - `calleeName` (String)
    - `phoneNumber` (String)
    - `subject` (String) quick description
    - `status` (String enum: `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`)
    - `scheduledAtEpoch` (Long)
    - `callAt` (Long)
    - `durationSec` (Integer, optional)
    - `aiLanguage` (String)
    - `voiceId` (String)
    - 
  - Post-call artifacts
    - `summary` (String, optional)
  - Secondary Indexes (recommended)
    - GSI `user-scheduled-index`: PK `userId`, SK `scheduledAtEpoch` (list upcoming)
    - GSI `user-start-index`: PK `userId`, SK `startAtEpoch` (list historical)

- Table: `callcat-transcripts` (from `aws.dynamodb.table.transcripts`)
  - Keys
    - Partition key: `callId` (String)
    - Sort key: `timestampEpoch` (Long)
  - Fields
    - `speakerType` (String enum: `USER`, `AI`)
    - `speakerName` (String, optional)
    - `text` (String)
    - `confidence` (Double, optional)
    - `language` (String, optional)

Notes
- Prefer epoch Longs for sort/range queries.
- Keep fields small where possible; transcripts store the heavy text.

---

### Calls Controller (MVP API)
All endpoints are user-scoped (authenticated user can only read/write own calls).

- POST `/api/calls` — create/schedule a call
  - Body: `{ subject, scheduledAtEpoch?, channel, direction, callee, notes?, tags? }`
  - Returns: `{ callId }`

- GET `/api/calls` — list calls with filters
  - Query params: `status?`, `fromEpoch?`, `toEpoch?`, `type?=scheduled|started|ended`, `limit?`, `cursor?`, `tags?`
  - Behavior: uses `user-scheduled-index` when `type=scheduled`, `user-start-index` when `type=started`/historical.

- GET `/api/calls/{callId}` — get a single call

- PATCH `/api/calls/{callId}` — edit mutable fields
  - Body (all optional): `{ subject?, scheduledAtEpoch?, channel?, direction?, callee?, notes?, tags?, status?, startAtEpoch?, endAtEpoch?, durationSec?, recordingUrl? }`
  - Validation rules apply (see below).

- DELETE `/api/calls/{callId}` — delete or cancel
  - MVP: soft-cancel if `status=SCHEDULED`; if already started, return 409.
  - Optionally support hard delete for testing/admin.

- POST `/api/calls/{callId}/reschedule` — convenience endpoint
  - Body: `{ scheduledAtEpoch }`

- POST `/api/calls/{callId}/finalize` — compute/store post-call artifacts
  - Body: `{ strategy?="llm"|"none" }`
  - Effect: reads transcript, generates `summary`, `actionItems`, `sentimentScore` and updates call; sets status `COMPLETED` if `endAtEpoch` present.

- POST `/api/calls/{callId}/tags` — add tags
  - Body: `{ tags: string[] }`

- DELETE `/api/calls/{callId}/tags/{tag}` — remove tag

Future (v1+)
- POST `/api/calls/{callId}/notes` — append internal note entries
- POST `/api/calls/{callId}/invite` — send calendar invite/reminders
- POST `/api/calls/{callId}/recording` — attach external recording metadata

---

### Transcript Controller (MVP API)

- POST `/api/calls/{callId}/transcripts`
  - Body: `{ entries: [{ timestampEpoch, speakerType, text, speakerName?, confidence?, language? }] }`
  - Appends one or many entries.

- GET `/api/calls/{callId}/transcripts`
  - Query: `fromTs?`, `limit?`, `cursor?`
  - Returns ordered segments, paginated by sort key.

---

### Validation & Rules
- `scheduledAtEpoch` must be in the future when `status=SCHEDULED`.
- `startAtEpoch <= endAtEpoch` and `durationSec = (end-start)` if not provided.
- Transitions allowed:
  - `SCHEDULED -> RINGING|IN_PROGRESS|CANCELED|NO_SHOW`
  - `RINGING -> IN_PROGRESS|FAILED`
  - `IN_PROGRESS -> COMPLETED|FAILED`
  - Final states: `COMPLETED|CANCELED|NO_SHOW|FAILED`
- On cancel: allowed only from `SCHEDULED`.

---

### Authorization & Security
- All routes require JWT auth; limit to current user’s data (`userId` from token).
- Input validation on size/length (subject, notes, tags, transcript text).
- Idempotency keys for create and transcript append (optional v1+).

---

### Pagination & Sorting
- Use cursor-based pagination for list endpoints.
- Sorting:
  - Upcoming: ascending `scheduledAtEpoch` via `user-scheduled-index`.
  - Historical: descending `startAtEpoch` via `user-start-index` (query reverse-order client-side if needed).

---

### Error Model (examples)
- 400: validation error (e.g., invalid transition)
- 401: unauthenticated
- 403: forbidden (call does not belong to user)
- 404: not found (callId)
- 409: conflict (cancel after start, overlapping reschedule if we enforce constraints)

---

### DTO Sketches (concise)
- ScheduleCallRequest
  - `{ subject, scheduledAtEpoch?, channel, direction, callee, notes?, tags? }`
- CallUpdateRequest
  - `{ subject?, scheduledAtEpoch?, channel?, direction?, callee?, notes?, tags?, status?, startAtEpoch?, endAtEpoch?, durationSec?, recordingUrl? }`
- TranscriptAppendRequest
  - `{ entries: [{ timestampEpoch, speakerType, text, speakerName?, confidence?, language? }] }`

---

### UI Mapping to Mock
- Scheduled cards → `GET /api/calls?type=scheduled`.
- Completed cards → `GET /api/calls?status=COMPLETED` or `type=started&toEpoch=now`.
- Transcript links → `GET /api/calls/{callId}/transcripts`.
- Reschedule buttons → `POST /api/calls/{callId}/reschedule` or `PATCH` with `scheduledAtEpoch`.

---

### Non-functional
- Observability: log call lifecycle transitions; metric counters per status.
- Audit trail: store `updatedBy`, `updatedAt` in call record metadata (optional).
- Rate limiting (basic global limit per user for create/append).
- Testing: controller unit tests + DynamoDB integration tests.

---

### Open Questions
- Multi-participant calls now or later? If yes, add `participants` list and richer transcript speaker IDs.
- External providers (Twilio, etc.) in v1? If yes, reserve fields for provider IDs and webhooks.
- Do we need calendar invites/reminders in MVP?

---

### Implementation Plan (incremental)
1) Expand DynamoDB entities (`CallRecord`, `CallTranscript`) with fields above; add enums.
2) Create repositories using Enhanced Client with table names from properties; define GSIs.
3) Implement services: `CallService`, `TranscriptService` with validation and transitions.
4) Implement controllers and DTOs for MVP endpoints.
5) Add tests for create/edit/delete/list, transcript append/list, and invalid transitions.
6) Add basic summary generator placeholder for `/finalize`.

---

### Example Call JSON (read model)
```json
{
  "callId": "cc-003",
  "userId": 42,
  "subject": "Sakura Sushi",
  "status": "SCHEDULED",
  "scheduledAtEpoch": 1737062400,
  "channel": "PHONE",
  "direction": "OUTBOUND",
  "callee": "+15551234567",
  "notes": "Reservation confirmed for 4 people at 8:00 PM tonight.",
  "tags": ["reservation", "dinner"],
  "summary": null,
  "actionItems": [],
  "sentimentScore": null
}
```


