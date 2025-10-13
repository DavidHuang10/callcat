# [call-cat.com](https://call-cat.com) 🐱

<img src="imgs/callcat.jpg" alt="CallCat" width="400">

## What is CallCat?

CallCat is a multilingual AI phone call platform that automates client outreach with the power of conversational AI. Instead of spending hours making appointment confirmations, follow-ups, and reminders, you can schedule AI-powered calls that handle everything for you.

Built for people who have better things to do than sit on hold or waiting for businesses to open to call them, just schedule a call! (Works excellent for lazy people too)
**Live at**: [call-cat.com](https://call-cat.com)

## Key Features

### Core Functionality
- **AI Phone Calls**: Schedule instant or future calls with custom prompts
- **Multilingual Support**: Use languages including English, Spanish, French, (Mandarin is coming soon!)
- **Smart Scheduling**: Calendar integration and invites
- **Call Analytics**: Success rates, duration tracking, and conversation insights


### Technical Highlights
- **AWS Infrastructure**: Serverless architecture with Lambda, DynamoDB, and RDS
- **Concurrent Processing**: Handles up to 20 simultaneous calls
- **JWT Authentication**: Secure user sessions with JWT token-based auth
- **Real-time Updates**: WebSocket connections for live call status
- **Production Ready**: Auto-scaling deployment on AWS Elastic Beanstalk

## Architecture

### Backend (Java/Spring Boot)
- **REST API**: Comprehensive endpoints for all operations
- **Database Layer**: PostgreSQL for users, DynamoDB for call records
- **Async Processing**: Multi-threaded call execution and transcript polling
- **AWS Integration**: Lambda functions for scheduled calls, S3 for recordings

### Frontend (React/Next.js)
- **Modern Stack**: Next.js 15, React 19, TypeScript, Tailwind CSS
- **Component Library**: Custom UI components built on Radix primitives
- **State Management**: Context API with custom hooks for data flow
- **Real-time UI**: Live updates for call status and transcripts

### Infrastructure
- **Serverless Scheduling**: AWS Lambda + EventBridge for timed execution
- **Scalable Storage**: DynamoDB for call data, S3 for audio files
- **Production Deployment**: Elastic Beanstalk with auto-scaling
- **HTTPS/Security**: SSL termination, CORS configuration, rate limiting

## What Made This Challenging

**Real-time Coordination**: Getting live transcripts from third-party APIs while managing concurrent calls was tricky. I ended up building a multi-threaded polling system that checks for transcript updates every few seconds without overwhelming the external service. Unfortunately, after switching providers for call services (VAPI was too expensive), I am no longer able to provide live transcripts, but the infrastructure that used to exist is still present for live transcript handling.

**Spring Boot*: Coming from simpler frameworks, Spring's dependency injection and auto-configuration felt magical but opaque. I spent a lot of time understanding how @Autowired actually works and when to use @Service vs @Component, and different syntaxes to use. Also, I found spring boot's database functionality especially with Relational Databases to be incredibly easy to program.
 
**AWS Service Orchestration**: Coordinating Lambda, DynamoDB, RDS, and Elastic Beanstalk was like conducting an orchestra where each instrument speaks a different language. Planning was definitely important here.

**Frontend State Management**: Managing complex form state for call creation while keeping everything type-safe in TypeScript pushed me to write custom hooks that actually made the code cleaner.

## What I Learned

### Technical Growth
**Async Programming**: Building the concurrent call system taught me a lot about thread safety, connection pooling, and graceful error handling. Java's CompletableFuture became my best friend.

**API Design**: Creating endpoints that work well for both the dashboard and potential integrations required thinking about data structures from multiple angles. I learned to design APIs that are intuitive but flexible.

**Database Optimization**: With call records potentially reaching thousands of rows, I had to think carefully about indexing strategies and query patterns. DynamoDB helped a lot with storing call records.

### Product Thinking
**User Experience**: The biggest lesson was that technical complexity should be invisible to users. The hardest engineering problems often result in the simplest user interfaces.

**Scalability Planning**: Building something that works for 10 calls vs 10,000 calls requires completely different approaches. I learned to plan for scale from day one, not as an afterthought.

### Development Process
**Testing Strategy**: With external API dependencies, I had to get creative with mocking and integration tests. The result was a test suite that actually catches bugs.

**Deployment Automation**: Setting up CI/CD pipelines that handle database migrations, environment configs, and zero-downtime deployments taught me to think like a DevOps engineer.

## Technical Stack

### Languages & Frameworks
- **Backend**: Java, Spring Boot, Spring Security, JPA/Hibernate
- **Frontend**: TypeScript, React 19, Next.js 15, Tailwind CSS
- **Database**: PostgreSQL, DynamoDB
- **Infrastructure**: AWS (Lambda, RDS, DynamoDB, S3, Elastic Beanstalk)

### Key Dependencies
- **AI Integration**: Retell AI for voice synthesis and conversation handling
- **Authentication**: JWT with Spring Security
- **UI Components**: Radix UI primitives
- **Build Tools**: Maven, npm, AWS CLI



## Contributing

This project started as a learning exercise but has grown into something that could genuinely help businesses. If you're interested in contributing, I'd love to collaborate on new features or optimizations.

Areas where help would be appreciated:
- Mobile app development (React Native)
- Additional language support
- Performance optimizations
- UI/UX improvements (i'm not that good)


Built with curiosity and way too much coffee by David Huang
