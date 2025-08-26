# AWS Setup for CallCat Scheduled Calls

## Overview
This document covers the AWS infrastructure setup needed for automatic call scheduling and live transcript polling.

## AWS Components Required
- **AWS Lambda**: Triggered by EventBridge to initiate scheduled calls
- **Amazon EventBridge**: Schedules one-time triggers for each call
- **IAM Permissions**: Allow Spring Boot to create/delete EventBridge rules and invoke Lambda

## Setup Steps (Do First Before AI Implementation)

### 1. Create Lambda Function
```bash
# Via AWS Console:
# Function name: callcat-scheduler-lambda
# Runtime: Java 17
# Architecture: x86_64
# Handler: com.callcat.scheduler.SchedulerLambda::handleRequest
```

**Lambda Function Code Structure** (placeholder):
```java
package com.callcat.scheduler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class SchedulerLambda implements RequestHandler<Map<String, Object>, String> {
    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        // Implementation will be added during AI implementation phase
        String callId = (String) event.get("callId");
        context.getLogger().log("Triggered for callId: " + callId);
        return "success";
    }
}
```

### 2. Lambda Environment Variables
Set these in Lambda console:
```bash
SPRING_BOOT_BASE_URL=https://your-api-domain.com
DYNAMODB_CALLS_TABLE=callcat-calls
DYNAMODB_TRANSCRIPTS_TABLE=callcat-transcripts
AWS_REGION=us-east-1
```

### 3. IAM Permissions for Spring Boot Application
Add these permissions to your existing AWS user/role that Spring Boot uses:

**EventBridge Permissions**:
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
    }
  ]
}
```

**Lambda Invocation Permissions**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "lambda:InvokeFunction", 
      "Resource": "arn:aws:lambda:*:*:function:callcat-scheduler-lambda"
    }
  ]
}
```

### 4. Lambda Execution Role Permissions
The Lambda function needs these permissions (create new role or modify existing):

**DynamoDB Access**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:GetItem",
        "dynamodb:Query",
        "dynamodb:UpdateItem"
      ],
      "Resource": [
        "arn:aws:dynamodb:*:*:table/callcat-calls",
        "arn:aws:dynamodb:*:*:table/callcat-calls/index/*"
      ]
    }
  ]
}
```

**Basic Lambda Execution**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*"
    }
  ]
}
```

**EventBridge Rule Cleanup** (so Lambda can clean up after itself):
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "events:DeleteRule",
        "events:RemoveTargets"
      ],
      "Resource": "arn:aws:events:*:*:rule/CallCat-*"
    }
  ]
}
```

### 5. Test Lambda Function
Create a test event in Lambda console:
```json
{
  "callId": "test-call-123"
}
```

Verify:
- Lambda executes without errors
- Can access DynamoDB tables
- Can make HTTP requests to external APIs

### 6. Network Configuration
If your Spring Boot app is not publicly accessible:
- Configure Lambda VPC settings to reach your application
- Ensure security groups allow Lambda → Spring Boot communication

## Verification Checklist
- [ ] Lambda function created with Java 17 runtime
- [ ] Environment variables set in Lambda
- [ ] Spring Boot has EventBridge permissions
- [ ] Spring Boot can invoke Lambda
- [ ] Lambda can read/write DynamoDB
- [ ] Lambda can reach Spring Boot API endpoint
- [ ] Test event executes successfully

## Cost Implications
- **EventBridge**: $1 per million events (very low cost)
- **Lambda**: Free tier covers 1M requests/month + compute time
- **Expected cost**: < $1/month for typical usage

## Notes
- EventBridge rules are created dynamically and deleted after execution
- Lambda only runs when triggered (no constant running costs)
- Each scheduled call creates one EventBridge rule + one Lambda execution