# Authentication System Overview

This document explains all the authentication-related files in our Spring Boot backend and what comes from Spring Boot vs. our custom implementation.

## Files Created for Authentication

### 1. Entity Layer

#### `src/main/java/com/callcat/backend/entity/User.java`
**What it is**: JPA entity representing a user in the database
**Spring Boot vs Custom**: 
- `@Entity`, `@Table`, `@Id`, `@GeneratedValue` - Spring Boot JPA annotations
- `UserDetails` interface implementation - Spring Security interface
- Custom fields: email, firstName, lastName, role enum

**Key Points**:
- Implements Spring Security's `UserDetails` interface
- Uses Spring Boot's JPA for database mapping
- Custom `Role` enum for user permissions

### 2. Repository Layer

#### `src/main/java/com/callcat/backend/repository/UserRepository.java`
**What it is**: Data access layer for User entity
**Spring Boot vs Custom**:
- `JpaRepository<User, Long>` - Spring Boot interface
- `findByEmail()` method - Custom query method (Spring Boot generates implementation)

### 3. Configuration Layer

#### `src/main/java/com/callcat/backend/config/SecurityConfig.java`
**What it is**: Main security configuration
**Spring Boot vs Custom**:
- `@Configuration`, `@EnableWebSecurity` - Spring Boot annotations
- `SecurityFilterChain` - Spring Security class
- `PasswordEncoder` bean - Spring Security, but we configure BCrypt
- `AuthenticationManager` bean - Spring Security, but we configure it
- Custom JWT filter registration and URL patterns

**Key Points**:
- Configures which endpoints require authentication
- Sets up password encoding (BCrypt)
- Registers our custom JWT filter
- Configures CORS for frontend integration

### 4. Security Layer (Custom JWT Implementation)

#### `src/main/java/com/callcat/backend/security/JwtService.java`
**What it is**: Handles JWT token creation and validation
**Spring Boot vs Custom**: **100% Custom Implementation**
- Uses external JWT library (io.jsonwebtoken)
- Custom methods for token generation, validation, and parsing
- Custom secret key and expiration configuration

**Key Methods**:
- `generateToken()` - Creates JWT with user email and expiration
- `isTokenValid()` - Validates token signature and expiration
- `extractUsername()` - Gets email from token claims

#### `src/main/java/com/callcat/backend/security/JwtAuthenticationFilter.java`
**What it is**: Filter that intercepts requests to validate JWT tokens
**Spring Boot vs Custom**: **Mostly Custom**
- Extends Spring Security's `OncePerRequestFilter`
- Custom logic for extracting and validating JWT from Authorization header
- Integrates with Spring Security's authentication context

**How it works**:
1. Intercepts every request
2. Extracts JWT from "Authorization: Bearer <token>" header
3. Validates token using JwtService
4. Sets authentication in Spring Security context

### 5. Service Layer

#### `src/main/java/com/callcat/backend/service/UserDetailsServiceImpl.java`
**What it is**: Loads user details for Spring Security
**Spring Boot vs Custom**:
- Implements Spring Security's `UserDetailsService` interface
- Custom logic to load user by email from database

#### `src/main/java/com/callcat/backend/service/AuthenticationService.java`
**What it is**: Business logic for registration and login
**Spring Boot vs Custom**: **Mostly Custom**
- Uses Spring Security's `AuthenticationManager` (configured in SecurityConfig)
- Custom registration logic with password encoding
- Custom login logic with JWT token generation

### 6. Controller Layer

#### `src/main/java/com/callcat/backend/controller/AuthController.java`
**What it is**: REST endpoints for authentication
**Spring Boot vs Custom**:
- `@RestController`, `@RequestMapping` - Spring Boot annotations
- Custom endpoints: `/register`, `/login`
- Custom request/response handling

### 7. DTOs (Data Transfer Objects)

#### `src/main/java/com/callcat/backend/dto/LoginRequest.java`
**What it is**: Request body for login endpoint
**Spring Boot vs Custom**: **100% Custom**

#### `src/main/java/com/callcat/backend/dto/RegisterRequest.java`
**What it is**: Request body for registration endpoint
**Spring Boot vs Custom**: **100% Custom**

#### `src/main/java/com/callcat/backend/dto/AuthResponse.java`
**What it is**: Response body containing JWT token and user info
**Spring Boot vs Custom**: **100% Custom**

#### `src/main/java/com/callcat/backend/dto/UserResponse.java`
**What it is**: User information without sensitive data
**Spring Boot vs Custom**: **100% Custom**

## What Comes From Where

### Spring Boot Provides:
- JPA annotations and repository interfaces
- Security annotations and base classes
- Password encoding utilities
- Authentication manager
- Filter chain infrastructure
- REST controller annotations

### Spring Security Provides:
- `UserDetails` and `UserDetailsService` interfaces
- `OncePerRequestFilter` base class
- `SecurityFilterChain` configuration
- Authentication context management

### Our Custom Implementation:
- JWT token generation and validation logic
- User entity structure and business logic
- Authentication endpoints and DTOs
- Security configuration rules
- Database schema and queries

## Authentication Flow

1. **Registration**: User submits email/password → Service encrypts password → Saves to database
2. **Login**: User submits credentials → AuthenticationManager validates → JwtService generates token → Returns token to client
3. **Protected Requests**: Client sends token in header → JwtAuthenticationFilter validates → Sets authentication context → Request proceeds

## Key Dependencies Added to pom.xml
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

The JWT implementation is entirely custom - Spring Boot doesn't provide JWT support out of the box, so we use the `io.jsonwebtoken` library and build our own JWT service and filter.