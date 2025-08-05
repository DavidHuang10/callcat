# CallCat Backend - Production Deployment Guide

## 🚀 Environment Variables (Required)

### JWT Configuration
```bash
# Required: Strong JWT secret (minimum 256 bits / 32+ characters)
export JWT_SECRET=4a718b39e18eb8410c9d82314c5d4d6f3a50447e88d79578f197ba2c1b8e994d

# Optional: Token expiration time in milliseconds (default: 24 hours)
export JWT_EXPIRATION=86400000
```

### Database Configuration (Production)
```bash
# PostgreSQL connection
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/callcat
export SPRING_DATASOURCE_USERNAME=callcat_user
export SPRING_DATASOURCE_PASSWORD=your_secure_password

# Database driver (automatically detected for PostgreSQL)
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# Hibernate settings for production
export SPRING_JPA_HIBERNATE_DDL_AUTO=validate
export SPRING_JPA_SHOW_SQL=false
export SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

### Server Configuration
```bash
# Production port (optional, default: 8080)
export SERVER_PORT=8080

# Profile
export SPRING_PROFILES_ACTIVE=production
```

## 🔐 Security Checklist

### ✅ Pre-Deployment Security
- [ ] JWT secret is 32+ characters and cryptographically random
- [ ] Database credentials are secure and not hardcoded
- [ ] Environment variables are set in deployment environment
- [ ] No sensitive data in application.properties
- [ ] HTTPS is configured in production
- [ ] CORS is properly configured for your frontend domain

### ✅ JWT Security
- [ ] Strong secret (current: 64 hex characters)
- [ ] Appropriate expiration time (current: 24 hours)
- [ ] Token validation is working
- [ ] No JWT secrets in source code or logs

## 🐳 Docker Deployment

### Dockerfile Example
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/backend-*.jar app.jar

# Environment variables will be passed at runtime
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

### Docker Compose with Environment Variables
```yaml
version: '3.8'
services:
  callcat-backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - JWT_EXPIRATION=${JWT_EXPIRATION}
      - SPRING_DATASOURCE_URL=${DATABASE_URL}
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - SPRING_PROFILES_ACTIVE=production
    depends_on:
      - postgres

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=callcat
      - POSTGRES_USER=callcat_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## ☁️ Cloud Deployment Examples

### Heroku
```bash
# Set environment variables
heroku config:set JWT_SECRET=4a718b39e18eb8410c9d82314c5d4d6f3a50447e88d79578f197ba2c1b8e994d
heroku config:set JWT_EXPIRATION=86400000
```

### AWS ECS/Fargate
```json
{
  "environment": [
    {
      "name": "JWT_SECRET",
      "value": "4a718b39e18eb8410c9d82314c5d4d6f3a50447e88d79578f197ba2c1b8e994d"
    },
    {
      "name": "JWT_EXPIRATION", 
      "value": "86400000"
    }
  ]
}
```

### Kubernetes

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: callcat-secrets
type: Opaque
stringData:
  jwt-secret: "4a718b39e18eb8410c9d82314c5d4d6f3a50647e88d79578f197ba2c1b8e994d"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: callcat-backend
spec: 
  template:
    spec:
      containers:
        - name: backend
          image: callcat/backend:latest
          env:
            - name: JWT_SECRET
              valueFrom:
                secretKeyRef:
                  name: callcat-secrets
                  key: jwt-secret



```

## 🏥 Health Checks

### Application Health Endpoint
Spring Boot automatically provides:
- `GET /actuator/health` - Application health status

### Environment Variable Validation
The application will:
- ✅ Use environment variables if set
- ✅ Fall back to defaults for development
- ✅ Log configuration on startup

## 🔧 Build Commands

### Maven Production Build
```bash
# Clean and build
./mvnw clean install

# Build without tests (faster)
./mvnw clean install -DskipTests

# Create executable JAR
./mvnw clean package
```

### JAR Execution
```bash
# With environment variables
JWT_SECRET=your_secret java -jar target/backend-*.jar

# With system properties
java -Djwt.secret=your_secret -jar target/backend-*.jar
```

## 📊 Monitoring & Logging

### Production Logging Configuration
```properties
# application-production.properties
logging.level.com.callcat.backend=INFO
logging.level.org.springframework.security=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

### Important: Never Log Sensitive Data
- JWT tokens should not appear in logs
- Passwords are automatically masked
- Database connection strings may contain credentials

## 🚨 Troubleshooting

### Common Issues
1. **JWT_SECRET not set**: Application uses default (insecure for production)
2. **Database connection failed**: Check DATABASE_URL and credentials
3. **Token validation errors**: Verify JWT_SECRET matches across instances
4. **CORS errors**: Configure allowed origins for your frontend

### Verification Commands
```bash
# Check environment variables are set
echo $JWT_SECRET
echo $JWT_EXPIRATION

# Test application startup
java -jar target/backend-*.jar --spring.profiles.active=production
```