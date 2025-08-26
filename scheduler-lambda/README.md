# CallCat Scheduler Lambda

## Overview
AWS Lambda function that triggers scheduled calls at their precise `scheduledFor` time using EventBridge + Lambda architecture.

## Project Structure
This is a **completely independent Maven project** separate from the Spring Boot backend.

### Key Features:
- ✅ **Independent Maven wrapper** (own `./mvnw`)
- ✅ **Java 17 runtime** (Lambda requirement)
- ✅ **Complete AWS SDK v2** dependencies
- ✅ **Fat JAR packaging** with Shade plugin
- ✅ **No dependency on backend project**

## Build & Development

### Prerequisites
- Java 17+ (Lambda runtime requirement)
- No additional Maven installation needed (uses wrapper)

### Build Commands
```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw test

# Package for deployment (creates fat JAR with all dependencies)
./mvnw package

# View dependency tree
./mvnw dependency:tree
```

### Deployment JAR
- **Location**: `target/scheduler-lambda-1.0.0.jar`
- **Size**: ~14MB (includes all AWS SDK dependencies)
- **Handler**: `com.callcat.scheduler.SchedulerLambda::handleRequest`

## IDE Setup

### IntelliJ IDEA
1. Import this directory as a separate Maven project
2. **File → Open** → Select `/lambda-scheduler/` 
3. Choose **"Import as Maven project"**
4. Set Project SDK to Java 17

### VS Code
1. Open this directory in VS Code
2. Java Extension Pack will auto-detect Maven project
3. Ensure Java 17 is configured

## AWS Lambda Configuration

### Runtime Settings
- **Runtime**: Java 17
- **Handler**: `com.callcat.scheduler.SchedulerLambda::handleRequest`
- **Memory**: 512MB (recommended)
- **Timeout**: 60 seconds

### Environment Variables
```bash
SPRING_BOOT_BASE_URL=https://your-ngrok-url.ngrok.io
DYNAMODB_CALLS_TABLE=callcat-calls
DYNAMODB_TRANSCRIPTS_TABLE=callcat-transcripts
AWS_REGION=us-east-1
```

### IAM Permissions Required
- DynamoDB: GetItem, Query, UpdateItem on `callcat-calls` and `callcat-transcripts`
- EventBridge: DeleteRule, RemoveTargets on rules prefixed with `CallCat-*`
- CloudWatch: CreateLogGroup, CreateLogStream, PutLogEvents

## Lambda Function Logic
1. **Receives EventBridge event** with `callId` in event detail
2. **Queries DynamoDB** using `byCallId` GSI to validate call is `SCHEDULED`
3. **HTTP POST** to Spring Boot `/api/calls/{callId}/trigger` endpoint
4. **Cleans up EventBridge rule** that triggered it (one-time execution)

## Dependencies
- AWS Lambda Java Core (1.2.3)
- AWS Lambda Java Events (3.11.3)  
- AWS SDK v2 DynamoDB (2.21.29)
- AWS SDK v2 EventBridge (2.21.29)
- Java 11+ HTTP Client (built-in)

## Development Notes
- **Completely separate** from `/backend` Spring Boot project
- **Independent build lifecycle** - can be built/tested without backend
- **No shared dependencies** or cross-project references
- **AWS SDK v2** for all AWS integrations