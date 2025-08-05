# Environment Variables Setup Guide

## 📁 **Files Created:**
- `.env` - Your actual environment variables (excluded from git)
- `.env.example` - Template file (committed to git)
- Updated `.gitignore` - Prevents committing sensitive `.env` files

## 🔧 **How to Load Environment Variables**

### **Option 1: Manual Export (Current Method)**
Load environment variables before running the application:

```bash
# Load from .env file
export $(cat .env | xargs)

# Then run the application
./mvnw spring-boot:run
```

### **Option 2: IDE Configuration**
**IntelliJ IDEA:**
1. Go to Run Configuration
2. Add Environment Variables:
   - `JWT_SECRET=4a718b39e18eb8410c9d82314c5d4d6f3a50447e88d79578f197ba2c1b8e994d`
   - `JWT_EXPIRATION=86400000`

**VS Code:**
1. Create `.vscode/launch.json`
2. Add environment variables in configuration

### **Option 3: Docker Compose (Recommended for Production)**
```yaml
# docker-compose.yml
version: '3.8'
services:
  callcat-backend:
    build: .
    env_file:
      - .env
    ports:
      - "8080:8080"
```

### **Option 4: Add Spring Boot Dotenv Support**
Add dependency to automatically load `.env` files:

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

## 🚨 **Security Best Practices**

### ✅ **Do's:**
- Keep `.env` in `.gitignore` 
- Use `.env.example` as a template
- Use different `.env` files for different environments
- Set environment variables in production deployment

### ❌ **Don'ts:**
- Never commit `.env` files to git
- Don't share `.env` files in plain text
- Don't use weak secrets in production

## 🎯 **Current Setup**

Your `.env` file contains:
```bash
JWT_SECRET=4a718b39e18eb8410c9d82314c5d4d6f3a50447e88d79578f197ba2c1b8e994d
JWT_EXPIRATION=86400000
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=development
```

## 🔄 **How to Use**

### **Development:**
```bash
# Load environment variables
export $(cat .env | xargs)

# Run application
./mvnw spring-boot:run

# Or run tests
./mvnw test
```

### **Production:**
Set environment variables directly in your deployment platform (Heroku, AWS, Docker, etc.)

## 🛠️ **Verification**

Test that environment variables are loaded:
```bash
# Check if variables are set
echo $JWT_SECRET
echo $JWT_EXPIRATION

# Run application and check logs for environment variable usage
```