# Gemini Agent Email Invite System

## Overview
An intelligent system that analyzes call transcripts using a Gemini agent with tools to autonomously send meeting invites when appropriate. The agent can understand natural language context and take direct actions through integrated tools.

## Architecture Vision

### Core Concept
Instead of rigid transcript parsing, we employ a **tool-enabled Gemini agent** that:
- Reads call transcripts in natural language
- Understands meeting context and intent
- Makes autonomous decisions about sending invites
- Has direct access to email, calendar, and contact systems
- Handles edge cases and variations naturally

### Technology Stack
- **LangChain4j** - Java-based LLM orchestration framework
- **Gemini Pro** - Google's advanced reasoning model
- **Tool Integration** - LangChain4j tool annotations for direct system access
- **Spring Boot** - Existing backend infrastructure integration

## Implementation Design

### Agent Tools
The Gemini agent has access to these tools for autonomous operation:

#### 1. EmailTool
```java
@Tool("Send email with optional calendar attachment")
public void sendEmail(String to, String subject, String body, String icsAttachment)
```
- Integrates with existing `EmailService` (AWS SES + Gmail)
- Supports .ics calendar file attachments
- Handles HTML email formatting

#### 2. CalendarTool
```java
@Tool("Generate calendar invite file for meetings")
public String generateCalendarInvite(String title, String dateTime, String location, String description, List<String> attendees)
```
- Creates RFC 5545 compliant .ics files
- Handles timezone conversions
- Generates unique event IDs

#### 3. ContactTool
```java
@Tool("Look up user email addresses and contact information")
public ContactInfo getUserContact(String name, String userId)
```
- Accesses existing user profile system
- Returns email addresses for invite recipients
- Supports fuzzy name matching

#### 4. TranscriptTool
```java
@Tool("Access call transcript content")
public String getTranscript(String callId)
```
- Retrieves transcript from existing `TranscriptService`
- Provides clean, formatted text to agent
- Includes speaker identification

### Workflow Architecture

```
Call Completed → call_ended webhook
       ↓
WebhookController (async)
       ↓
TranscriptAnalysisService
       ↓
Gemini Agent + Tools
       ↓
Agent Decision: Meeting mentioned?
       ↓
[YES] → Use tools to send invites
       ↓
Update callAnalyzed = true
```

### Agent Prompt Strategy
The Gemini agent operates with this system prompt:

```
You are an intelligent meeting assistant that analyzes phone call transcripts. 

Your goal: Identify when calls mention meetings, appointments, or events that require calendar invites.

Available tools:
- EmailTool: Send emails with calendar attachments
- CalendarTool: Generate .ics meeting files
- ContactTool: Look up user email addresses  
- TranscriptTool: Access call transcripts

When you find a meeting mentioned:
1. Extract: title, date/time, location, attendees
2. Generate calendar invite using CalendarTool
3. Look up attendee emails using ContactTool
4. Send invites using EmailTool

Be conservative - only send invites for clear, definitive meetings with specific dates/times.
```

## Integration Points

### Spring Boot Integration
- **Existing Services**: Leverages `EmailService`, `TranscriptService`, `CallService`
- **Database**: Updates `callAnalyzed` flag in existing DynamoDB structure
- **Authentication**: Uses existing user context for sender information
- **Async Processing**: Non-blocking to prevent webhook timeouts

### Error Handling & Reliability
- **Agent Retries**: Built-in retry logic for tool failures
- **Graceful Degradation**: System continues if analysis fails
- **Logging**: Comprehensive logging of agent decisions and actions
- **Fallback**: Manual analysis available if agent is unavailable

## Advanced Capabilities

### Natural Language Understanding
The agent can handle various meeting formats:
- "Let's schedule a follow-up for next Tuesday at 2 PM"
- "Can you come to our office meeting on Friday?"
- "I'll send you a calendar invite for the presentation"
- "We need to meet again to discuss the proposal"

### Smart Context Awareness
- Distinguishes between definitive meetings vs. casual mentions
- Extracts implied information (company addresses, typical meeting durations)
- Handles timezone references and business hour assumptions
- Understands participant roles and appropriate invite lists

### Multi-Modal Extensions (Future)
- **Voice Analysis**: Detect meeting urgency from tone
- **Contact Integration**: Sync with external address books
- **CRM Integration**: Link meetings to customer records
- **Calendar Sync**: Two-way sync with Google/Outlook calendars

## Configuration & Environment

### Required Environment Variables
```bash
# Gemini API Configuration
GEMINI_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-pro
GEMINI_TEMPERATURE=0.3

# Agent Configuration  
AGENT_MAX_ITERATIONS=5
AGENT_TIMEOUT_SECONDS=30
MEETING_ANALYSIS_ENABLED=true
```

### Spring Boot Configuration
```properties
# Agent settings
gemini.agent.enabled=true
gemini.agent.model=gemini-pro
gemini.agent.temperature=0.3
gemini.agent.max-tokens=2000

# Tool settings
tools.email.enabled=true
tools.calendar.enabled=true
tools.contact.enabled=true
```

## Benefits & Advantages

### Vs. Traditional Parsing
- **Flexible**: Handles natural language variations
- **Contextual**: Understands implied meaning and context
- **Extensible**: Easy to add new capabilities
- **Robust**: Handles edge cases and ambiguous language

### Vs. Lambda Approach  
- **Integrated**: Uses existing backend infrastructure
- **Efficient**: No cold start delays
- **Maintainable**: Single codebase, consistent patterns
- **Cost-Effective**: No additional AWS Lambda charges

### Business Value
- **Automated Follow-up**: Ensures no meetings are missed
- **Professional Experience**: Seamless calendar integration
- **Time Savings**: Eliminates manual invite creation
- **Scalable**: Handles any volume of calls

## Future Enhancements

### Phase 1: Core Implementation ✅
- Basic meeting detection and invite sending
- Integration with existing email system
- Simple calendar file generation

### Phase 2: Intelligence Improvements
- Multi-language support for international calls
- Smart attendee discovery from transcript context
- Meeting type classification (internal, client, follow-up)
- Automatic meeting duration estimation

### Phase 3: External Integrations
- Google Calendar/Outlook bidirectional sync
- CRM system integration (Salesforce, HubSpot)
- Slack/Teams meeting notifications
- SMS reminders for important meetings

### Phase 4: Advanced Analytics
- Meeting frequency and success tracking
- Agent performance optimization
- Custom business rule integration
- Predictive meeting scheduling

## Development Approach

### Implementation Priority
1. **Tool Development** - Build individual tools first
2. **Agent Integration** - Connect Gemini with LangChain4j
3. **Webhook Integration** - Trigger agent from call completion
4. **Testing & Refinement** - Iterate on agent prompts and tools
5. **Production Deployment** - Monitor and optimize performance

### Testing Strategy
- **Unit Tests**: Individual tool functionality
- **Integration Tests**: Agent decision-making scenarios
- **End-to-End Tests**: Complete transcript-to-email flow
- **Load Testing**: Concurrent call processing
- **Edge Case Testing**: Ambiguous language and error conditions

## Success Metrics

### Technical Metrics
- **Accuracy**: Percentage of correctly identified meetings
- **Precision**: No false positive invite sending
- **Performance**: Analysis completion time < 30 seconds
- **Reliability**: 99.9% uptime for analysis service

### Business Metrics
- **User Satisfaction**: Feedback on invite relevance and timing
- **Time Savings**: Reduction in manual calendar management
- **Meeting Follow-through**: Increase in scheduled meeting attendance
- **System Adoption**: Percentage of users with analysis enabled

This document serves as the architectural blueprint for building an intelligent, autonomous meeting management system that transforms call transcripts into actionable calendar invites through advanced AI agent capabilities.