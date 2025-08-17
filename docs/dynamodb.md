# DynamoDB Call Storage — Recommended Design

This design uses `scheduledFor` (Number, epoch **ms**) and composite fields for queries. It also explains how to keep those fields up to date.

---

## 1) Access Patterns

- Store all calls per user, sorted by time.
- Direct lookup by `callId`.
- Lookup by `providerId`.
- Per-user timelines:
    - Upcoming (`SCHEDULED`) → ascending by time.
    - Completed (`COMPLETED`) → descending by time.

---

## 2) Table & Indexes

### Base Table: `Calls`
- **PK**: `userId` (S)
- **SK**: `sk` (S) → `String.format("%013d#%s", scheduledForMs, callId)`
- **Attributes**:
    - `callId` (S, unique)
    - `providerId` (S)
    - `status` (S: `SCHEDULED` | `COMPLETED`)
    - `scheduledFor` (N, epoch ms)
    - `userStatus` (S) = `userId#status`

### GSI 1 — `byCallId`
- **PK**: `callId`

### GSI 2 — `byProvider`
- **PK**: `providerId`
- **SK**: `sk` (optional if providerId truly unique per call)

### GSI 3 — `byUserStatus`
- **PK**: `userStatus` (`userId#status`)
- **SK**: `sk`
- Query ascending for upcoming, descending for completed (set `ScanIndexForward`).

---

## 3) Composite Fields

- **`sk`**: `"%013d#%s" → scheduledForMs padded + callId`
- **`userStatus`**: `userId#status`

These must be computed client-side for every write.

---

## 4) Updates

- **Create**: compute `sk` + `userStatus` and `PutItem`. Use condition `attribute_not_exists(userId) AND attribute_not_exists(sk)` for idempotency.
- **Status change**: update `status` and `userStatus` together. Use conditions to avoid race overwrites.
- **Reschedule (`scheduledFor` change)**: PK changes → do a `TransactWrite` (Put new + Delete old).
- **Reassign user**: same as reschedule.
- **Provider change**: simple update (Dynamo moves the GSI entry).
- **Concurrency**: use version attributes for optimistic locking if needed.

---

## 5) Query Examples

- **Next scheduled call**: `byUserStatus` with `PK=userId#SCHEDULED`, `SK >= now`, ascending, `Limit=1`.
- **Last completed call**: `byUserStatus` with `PK=userId#COMPLETED`, descending, `Limit=1`.
- **Provider’s calls**: `byProvider` with `PK=providerId`, descending.
- **Direct lookup**: `byCallId` with `PK=callId`.

---

## 6) AWS CLI Create Table

```bash
aws dynamodb create-table \
  --table-name Calls \
  --attribute-definitions \
      AttributeName=userId,AttributeType=S \
      AttributeName=sk,AttributeType=S \
      AttributeName=callId,AttributeType=S \
      AttributeName=providerId,AttributeType=S \
      AttributeName=userStatus,AttributeType=S \
  --key-schema \
      AttributeName=userId,KeyType=HASH \
      AttributeName=sk,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --global-secondary-indexes '[
    {
      "IndexName": "byCallId",
      "KeySchema": [{"AttributeName": "callId", "KeyType": "HASH"}],
      "Projection": {"ProjectionType": "ALL"}
    },
    {
      "IndexName": "byProvider",
      "KeySchema": [
        {"AttributeName": "providerId", "KeyType": "HASH"},
        {"AttributeName": "sk", "KeyType": "RANGE"}
      ],
      "Projection": {"ProjectionType": "ALL"}
    },
    {
      "IndexName": "byUserStatus",
      "KeySchema": [
        {"AttributeName": "userStatus", "KeyType": "HASH"},
        {"AttributeName": "sk", "KeyType": "RANGE"}
      ],
      "Projection": {"ProjectionType": "ALL"}
    }
  ]'
```
7) Key Builders (Java Example)
   static String sk(long scheduledForMs, String callId) {
   return String.format("%013d#%s", scheduledForMs, callId);
   }
   static String userStatus(String userId, String status) {
   return userId + "#" + status;
   }