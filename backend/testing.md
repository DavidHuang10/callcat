# CallCat Backend Testing Guide

## Running JUnit Tests

### Command Line Testing
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthenticationServiceTest

# Run tests with verbose output
./mvnw test -Dtest=AuthControllerTest -Dmaven.test.failure.ignore=true

# Run tests and generate coverage report
./mvnw clean test jacoco:report
```

### Test Coverage
The test suite covers:
- **AuthenticationService**: Registration, login, password validation, user retrieval
- **AuthController**: All REST endpoints with various scenarios
- **JwtService**: Token generation, validation, claim extraction

## Manual API Testing with Postman

### Prerequisites
1. Start the Spring Boot application: `./mvnw spring-boot:run`
2. Application runs on `http://localhost:8080`
3. H2 database console (development): `http://localhost:8080/h2-console`

### Postman Collection Setup

#### 1. Registration Endpoint
**POST** `http://localhost:8080/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
    "email": "john.doe@example.com",
    "password": "StrongPass123",
    "firstName": "John",
    "lastName": "Doe"
}
```

**Expected Response (200):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "expirationTime": 86400000
}
```

#### 2. Login Endpoint
**POST** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
    "email": "john.doe@example.com",
    "password": "StrongPass123"
}
```

**Expected Response (200):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "expirationTime": 86400000
}
```

#### 3. Get Current User (Protected Endpoint)
**GET** `http://localhost:8080/api/auth/me`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Expected Response (200):**
```json
{
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "createdAt": "2024-01-15T10:30:00"
}
```

#### 4. Password Validation
**POST** `http://localhost:8080/api/auth/validate-password`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
    "password": "TestPassword123"
}
```

**Expected Response (200):**
```json
{
    "valid": true,
    "message": "Password is strong"
}
```

### Test Scenarios

#### Authentication Flow Testing
1. **Happy Path:**
   - Register new user → Login → Access protected endpoint
   - Verify JWT token is returned and works for authentication

2. **Error Cases:**
   - Register with existing email (400 error)
   - Login with wrong password (400 error)
   - Access protected endpoint without token (401 error)
   - Access protected endpoint with invalid token (401 error)

3. **Password Validation:**
   - Strong password: `StrongPass123` → valid: true
   - Weak password: `weak` → valid: false
   - No uppercase: `strongpass123` → valid: false
   - No numbers: `StrongPassword` → valid: false

#### Postman Environment Variables
Create a Postman environment with:
```
base_url = http://localhost:8080
jwt_token = {{token_from_login_response}}
```

#### Automated Test Scripts
Add to Postman test scripts:

**For Login/Register requests:**
```javascript
// Save token for subsequent requests
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.environment.set("jwt_token", response.token);
}
```

**For protected endpoints:**
```javascript
// Set Authorization header automatically
pm.request.headers.add({
    key: 'Authorization',
    value: 'Bearer ' + pm.environment.get("jwt_token")
});
```

### Browser Testing

#### CORS Testing
1. Open browser console at `http://localhost:3000`
2. Test CORS with fetch requests:

```javascript
// Should work (allowed origin)
fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        email: 'test@example.com',
        password: 'password'
    })
});

// Should fail from other origins
```

#### JWT Storage Testing
```javascript
// Store JWT in localStorage
localStorage.setItem('jwt_token', 'your-jwt-token');

// Include in requests
fetch('http://localhost:8080/api/auth/me', {
    headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('jwt_token')
    }
});
```

## Database Testing

### H2 Console Access
1. Navigate to `http://localhost:8080/h2-console`
2. Use connection settings:
   - **JDBC URL**: `jdbc:h2:mem:testdb`
   - **User Name**: `sa`
   - **Password**: (leave empty)

### Database Queries for Testing
```sql
-- View all users
SELECT * FROM users;

-- Check user roles
SELECT email, first_name, last_name, role, is_active FROM users;

-- Verify password encoding
SELECT email, password FROM users WHERE email = 'test@example.com';
```

## Integration Testing

### Full Authentication Flow Test
1. **Start fresh** (restart application to clear H2 database)
2. **Register** new user via API
3. **Verify** user exists in H2 database
4. **Login** with same credentials
5. **Access** protected endpoint with JWT
6. **Test** invalid scenarios (wrong password, expired token, etc.)

### Load Testing with Postman
1. Create Postman collection with all endpoints
2. Use Postman Runner for batch testing
3. Test concurrent registrations and logins
4. Monitor response times and error rates

## Common Issues & Troubleshooting

### Test Failures
- **Connection refused**: Ensure application is running on port 8080
- **401 Unauthorized**: Check JWT token format and expiration
- **CORS errors**: Verify origin matches security configuration
- **Database errors**: Restart application to reset H2 database

### JWT Token Issues
- **Token format**: Must be `Bearer <token>` in Authorization header
- **Token expiration**: Default 24 hours (86400000 ms)
- **Invalid signature**: Check JWT secret configuration

### Password Validation
- **Minimum 8 characters**
- **At least one uppercase letter**
- **At least one lowercase letter**
- **At least one number**

## Environment Configuration

### Development (H2)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### Testing Properties
```properties
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
```