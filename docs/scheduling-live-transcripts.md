# CallCat - Scheduled Calls & Live Transcripts Implementation Plan

## Current State ✅
- **CallRecord Entity**: DynamoDB table `callcat-calls` with composite key `userId + sk(scheduledForMs#callId)`
- **Call Creation**: `CallService.createCall()` stores calls as `SCHEDULED` with `scheduledFor` epoch timestamp
- **Retell Integration**: `RetellService.makeCall(callId)` creates Retell calls with metadata containing our `callId`
- **Webhook System**: `WebhookController` handles `call_started`, `call_ended`, `call_analyzed` events
- **Transcript Storage**: `CallTranscript` entity in DynamoDB table `callcat-transcripts` with 90-day TTL

## Missing Implementation

### 1. Scheduled Call Triggering (AWS Lambda + EventBridge)
**Current Gap**: No automatic execution of scheduled calls when `scheduledFor` time arrives

**Implementation Plan**:

1. **CallService Enhancement** (Spring Boot):
   ```java
   // In CallService.createCall() - add EventBridge scheduling
   public CallResponse createCall(String userEmail, CallRequest request) {
       // ... existing code ...
       callRecord = callRecordRepository.save(callRecord);
       
       // NEW: Schedule EventBridge rule for this call
       scheduleCallTrigger(callRecord.getCallId(), callRecord.getScheduledFor());
       
       return response;
   }
   
   private void scheduleCallTrigger(String callId, Long scheduledFor) {
       // Create one-time EventBridge rule with Lambda target
       // Rule name: "CallCat-" + callId
       // Schedule expression: "at(" + Instant.ofEpochMilli(scheduledFor) + ")"
       // Target: CallCat Lambda with event payload: {"callId": callId}
   }
   ```

2. **AWS Lambda Function** (`callcat-scheduler-lambda`):
   ```java
   // Lambda handler triggered by EventBridge
   public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> event) {
       String callId = (String) event.get("callId");
       
       // 1. Fetch call from DynamoDB using CallRecord GSI byCallId
       // 2. Validate call is still SCHEDULED
       // 3. Call RetellService.makeCall(callId) via HTTP to Spring Boot
       // 4. Start live transcript polling (trigger async service)
       // 5. Clean up EventBridge rule
       // remember that callId and providerId are separate, callId is callcat's internal
        // retell's get will have their own callId which is actually providerId, and we stored callId (ours) in metdata
   }
   ```

3. **DynamoDB Access Pattern**:
   - Lambda uses `byCallId` GSI on `callcat-calls` table
   - Query: `callId = "uuid"` returns single CallRecord
   - Update status from `SCHEDULED` to processing state if needed

### 2. Live Transcript Polling (Spring Boot Async)
**Current Gap**: No real-time transcript fetching during active calls

**Implementation Plan**:

1. **New TranscriptPollingService**:
   ```java
   @Service
   public class TranscriptPollingService {
       
       @Async("transcriptTaskExecutor")
       public CompletableFuture<Void> startPolling(String providerId) {
           CallRecord call = callService.findCallByProviderId(providerId);
           
           while (!"COMPLETED".equals(call.getStatus())) {
               // Poll Retell API: GET /get-call/{providerId}
               JsonNode callData = retellService.getCall(providerId);
               String transcript = extractTranscript(callData);
               
               if (!transcript.isEmpty()) {
                   transcriptService.saveTranscript(providerId, transcript);
               }
               
               Thread.sleep(2000); // Poll every 2 seconds
               call = callService.findCallByProviderId(providerId); // Check status
           }
           
           return CompletableFuture.completedFuture(null);
       }
   }
   ```

2. **Async Configuration**:
   ```java
   @Configuration
   @EnableAsync
   public class AsyncConfig {
       @Bean("transcriptTaskExecutor")
       public TaskExecutor transcriptTaskExecutor() {
           ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
           executor.setCorePoolSize(5);
           executor.setMaxPoolSize(20);
           executor.setQueueCapacity(100);
           executor.setThreadNamePrefix("transcript-");
           return executor;
       }
   }
   ```

3. **Integration Points**:
   - **CRITICAL**: Must wait for `RetellService.makeCall()` to return `providerId` before starting polling
   - Lambda flow: `makeCall(callId)` → get `providerId` → `startPolling(providerId)`
   - Stop polling when `WebhookController.handleCallEnded()` sets status to `COMPLETED`
   - Store incremental transcripts in existing `CallTranscript` table

### 3. Existing API Already Supports Live Transcripts ✅

**Current Transcript Endpoint** (No changes needed):
- `GET /api/live_transcripts/{providerId}` via `TranscriptController`
- Uses existing `TranscriptService.getTranscriptByProviderId()`
- Returns live/final transcripts from DynamoDB `callcat-transcripts` table

## Database Schema (Existing - No Changes Needed) ✅

**DynamoDB Tables**:
- `callcat-calls`: Stores CallRecord with `scheduledFor` field
- `callcat-transcripts`: Stores live/final transcripts with providerId key
- Both tables already support the required access patterns

**Key Fields**:
- `CallRecord.scheduledFor`: Epoch timestamp for EventBridge scheduling
- `CallRecord.providerId`: Retell call ID for transcript association
- `CallRecord.status`: `SCHEDULED` → `COMPLETED` state management

## Data Flow (Updated with Implementation Details)

```
1. User creates call via POST /api/calls
   ↓
2. CallService.createCall() saves to DynamoDB callcat-calls table
   - Uses composite key: userId + "scheduledForMs#callId" 
   - Creates EventBridge rule for scheduledFor timestamp
   ↓
3. EventBridge triggers Lambda at exact scheduled time
   - Lambda queries DynamoDB using byCallId GSI
   ↓
4. Lambda calls Spring Boot RetellService.makeCall(callId)
   - Updates CallRecord with providerId from Retell response
   - **CRITICAL**: Must wait for providerId before starting polling
   - Starts TranscriptPollingService.startPolling(providerId)
   ↓
5. Async thread polls Retell GET /get-call/{providerId} every 2s
   - Saves incremental transcripts to callcat-transcripts table using providerId key
   ↓ 
6. Frontend polls existing GET /api/live_transcripts/{providerId}
   - Gets providerId from CallRecord.providerId field 
   - Returns live transcript from DynamoDB callcat-transcripts table
   ↓
7. Retell webhook call_ended updates status to COMPLETED
   - Stops transcript polling thread
   - Final transcript stored via webhook call_analyzed
```

## Environment Configuration (Required)

**Additional AWS Permissions**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow", 
      "Action": [
        "events:PutRule",
        "events:PutTargets", 
        "events:DeleteRule",
        "events:RemoveTargets"
      ],
      "Resource": "arn:aws:events:*:*:rule/CallCat-*"
    },
    {
      "Effect": "Allow",
      "Action": "lambda:InvokeFunction", 
      "Resource": "arn:aws:lambda:*:*:function:callcat-scheduler-lambda"
    }
  ]
}
```

**Lambda Environment Variables**:
```bash
SPRING_BOOT_BASE_URL=https://your-api.com
DYNAMODB_CALLS_TABLE=callcat-calls
DYNAMODB_TRANSCRIPTS_TABLE=callcat-transcripts
```

## Implementation Priority
1. **Phase 1**: EventBridge scheduling in CallService + basic Lambda function
2. **Phase 2**: TranscriptPollingService with async thread management  
3. **Phase 3**: Frontend integration with existing `/api/live_transcripts/{providerId}` endpoint
4. **Phase 4**: Error handling, retry logic, and monitoring

