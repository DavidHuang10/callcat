# CallCat Frontend - JWT Authentication Integration

## Current Status
- **Framework**: Next.js with TypeScript
- **Styling**: TailwindCSS + shadcn/ui components
- **State**: No authentication state management yet
- **Routing**: Basic Next.js routing (needs protection)

## Backend API Endpoints Available

### Authentication Endpoints
```
POST /api/auth/register
POST /api/auth/login  
GET  /api/auth/me
POST /api/auth/validate-password
```

### API Response Format
```typescript
// Login/Register Success Response
{
  token: string,           // JWT token
  type: "Bearer",          // Token type
  userId: number,          // User ID
  email: string,           // User email
  fullName: string,        // User's full name
  expiresIn: number        // Token expiration time in ms (24 hours)
}

// Error Response
{
  message: string          // Error description
}
```

## Frontend Implementation Requirements

### 1. Authentication Context & State Management

**Options:**
- **React Context** (Simple, built-in)
- **Zustand** (Lightweight state management)
- **Redux Toolkit** (Full-featured but complex)

**Recommended: React Context** for simplicity

```typescript
// Needed: AuthContext with:
interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (userData: RegisterData) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
}
```

### 2. Token Storage Strategy

**Options:**
- **localStorage**: Simple but vulnerable to XSS attacks
- **sessionStorage**: More secure than localStorage, cleared on tab close
- **httpOnly Cookies**: Most secure, handled by browser automatically
- **Memory only**: Most secure but user logs out on refresh

**Recommended: httpOnly Cookies** for production, localStorage for development simplicity

```typescript
// If using localStorage (development):
localStorage.setItem('callcat_token', token);

// If using httpOnly cookies (production):
// Backend sets cookie, frontend doesn't handle token directly
```

### 3. HTTP Client Configuration

**Options:**
- **Built-in fetch** with custom wrapper
- **Axios** with interceptors
- **SWR** or **React Query** for data fetching + caching

**Recommended: Axios with interceptors**

```typescript
// Needed: API client that automatically:
// 1. Adds Authorization header to requests
// 2. Handles token expiration (redirect to login)
// 3. Refreshes tokens if implemented
```

### 4. Form Management

**Options:**
- **react-hook-form** (Popular, good validation)
- **Formik** (Full-featured)
- **Built-in React state** (Simple but verbose)

**Recommended: react-hook-form** with Zod validation

### 5. Password Validation
Real-time password strength checking using the `/api/auth/validate-password` endpoint.

**Requirements:**
- 8+ characters minimum
- At least one uppercase letter
- At least one lowercase letter  
- At least one number
- Visual feedback (strength meter/indicators)

### 6. Route Protection

**Implementation needed:**
```typescript
// Higher-order component or hook for protected routes
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }
  
  return children;
};
```

### 7. Components to Build

#### Authentication Forms
- **LoginForm** - Email/password login
- **RegisterForm** - Full registration with validation
- **PasswordStrengthIndicator** - Real-time password validation

#### Layout Components  
- **AuthGuard** - Wrapper for protected routes
- **Navigation** - Show user info, logout button when authenticated
- **LoadingSpinner** - For authentication state loading

#### Pages
- **LoginPage** (`/login`)
- **RegisterPage** (`/register`) 
- **Dashboard** (`/dashboard`) - Protected route
- **Profile** (`/profile`) - User profile management

### 8. Error Handling Strategy

**Requirements:**
- Global error boundary for unhandled errors
- Authentication error handling (token expiration)
- Form validation errors
- Network error handling
- User-friendly error messages

### 9. Security Considerations

**Frontend Security Measures:**
- Validate all inputs before sending to backend
- Don't store sensitive data in localStorage if possible
- Clear tokens on logout
- Handle token expiration gracefully
- Sanitize user inputs to prevent XSS

**Token Management:**
- Check token expiration before API calls
- Automatic logout on token expiration
- Clear all user data on logout

### 10. Development Setup

**Dependencies to Add:**
```json
{
  "axios": "^1.6.0",                    // HTTP client
  "react-hook-form": "^7.48.0",         // Form management
  "@hookform/resolvers": "^3.3.0",      // Form validation
  "zod": "^3.22.0",                     // Schema validation
  "js-cookie": "^3.0.5",                // Cookie management (if using cookies)
  "@types/js-cookie": "^3.0.6"          // TypeScript types
}
```

**Environment Variables:**
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 11. User Experience Flow

#### Registration Flow
1. User fills registration form
2. Real-time password validation
3. Form submission to `/api/auth/register`
4. Auto-login on successful registration
5. Redirect to dashboard

#### Login Flow  
1. User enters credentials
2. Form submission to `/api/auth/login`
3. Store token and user data
4. Redirect to dashboard or intended page

#### Protected Route Access
1. Check authentication status
2. If not authenticated, redirect to login
3. If authenticated, allow access
4. Handle token expiration during session

### 12. Testing Strategy

**Test Coverage Needed:**
- Authentication context behavior
- Form validation logic
- API integration tests
- Protected route access
- Token expiration handling
- User registration/login flows

### 13. Performance Considerations

- Lazy load authentication-related components
- Cache user data to avoid repeated API calls
- Efficient re-renders in auth context
- Optimize bundle size with code splitting

### 14. Accessibility

- Proper form labels and ARIA attributes
- Keyboard navigation support
- Screen reader compatibility
- Error message accessibility
- Focus management on route changes

## Next Steps Priority

1. **High Priority**
   - Set up authentication context
   - Create login/register forms
   - Implement token storage
   - Add route protection

2. **Medium Priority**
   - Error handling
   - Password strength validation
   - User profile management
   - Logout functionality

3. **Low Priority**
   - Advanced security features
   - Performance optimizations
   - Comprehensive testing
   - Accessibility improvements

## Integration with Backend

The backend is fully implemented with:
-  JWT token generation (24-hour expiration)
-  Password validation (8+ chars, upper, lower, numbers)
-  BCrypt password hashing (strength 12)
-  CORS configured for localhost:3000
-  User registration and authentication
-  Protected endpoint examples

Frontend just needs to integrate with these existing, secure endpoints.