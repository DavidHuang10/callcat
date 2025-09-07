# CallCat Frontend Deployment Guide

## Quick Deployment to Vercel

### 1. Prerequisites
- Vercel account (free tier available)
- GitHub repository with your code
- Node.js 18+ locally for testing

### 2. Deploy to Vercel (Simplest Method)

#### Option A: Vercel CLI (Recommended)
```bash
# Install Vercel CLI
npm i -g vercel

# Navigate to frontend directory
cd frontend/callcat

# Deploy
vercel

# Follow prompts:
# - Connect to GitHub (recommended)
# - Choose project name
# - Confirm build settings
```

#### Option B: Vercel Dashboard
1. Go to [vercel.com](https://vercel.com)
2. Connect your GitHub repository
3. Select `frontend/callcat` as root directory
4. Deploy automatically detects Next.js

### 3. Environment Variables Setup
Add these in Vercel dashboard under Project Settings > Environment Variables:

```bash
NEXT_PUBLIC_API_URL=https://api.call-cat.com
```

### 4. Custom Domain (Optional)
- Add custom domain in Vercel dashboard
- Update DNS records as instructed
- SSL automatically handled by Vercel

### 5. Automatic Deployments
- Push to main branch = automatic production deployment
- Push to other branches = preview deployments

## Expected Changes Required

### Backend Changes (Critical)

#### 1. CORS Configuration ⚠️ **REQUIRED**
**Current CORS allows:**
```
http://localhost:*
https://call-cat.com
https://*.call-cat.com
https://*.ngrok.io
```

**Need to add Vercel domains:**
```
https://your-app-name.vercel.app
https://your-app-name-*.vercel.app  # For preview deployments
```

**Action:** Update CORS configuration in your backend to include:
- `https://your-vercel-domain.vercel.app`
- `https://*.vercel.app` (for preview branches)

#### 2. Webhook URLs (If Using Webhooks)
- Update any webhook callback URLs to point to new domain
- Replace localhost URLs with production Vercel URL
- Update in Retell AI dashboard or other webhook providers

### Frontend Changes (Minor)

#### 1. Environment Variables ✅ **READY**
Current setup already uses environment variables:
```javascript
// lib/api.ts already uses
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'https://api.call-cat.com'
```

#### 2. Build Configuration ✅ **READY**
Next.js configuration already optimized:
- App Router architecture
- Static optimization enabled
- No additional Vercel config needed

#### 3. API Client ✅ **READY**
API service already configured for production:
```javascript
// Automatically uses correct base URL
// Handles CORS credentials properly
// Uses HttpOnly cookies for auth
```

### Security Considerations

#### 1. Authentication Cookies
Ensure backend sets cookies with correct domain:
```javascript
// Backend should set cookies for:
domain: '.call-cat.com'  // If using subdomain
domain: 'your-vercel-domain.vercel.app'  // For Vercel
```

#### 2. Content Security Policy
Consider adding CSP headers in `next.config.js` if needed:
```javascript
// next.config.js
const nextConfig = {
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
        ],
      },
    ]
  },
}
```

## Deployment Checklist

### Pre-Deployment
- [ ] Test build locally: `npm run build`
- [ ] Verify environment variables
- [ ] Update backend CORS settings
- [ ] Test API connectivity

### Post-Deployment  
- [ ] Verify login/logout functionality
- [ ] Test call creation
- [ ] Check API endpoints respond correctly
- [ ] Verify webhook callbacks (if applicable)

## Troubleshooting

### Common Issues
1. **CORS Errors**: Update backend CORS configuration
2. **API Not Found**: Check `NEXT_PUBLIC_API_URL` environment variable
3. **Build Failures**: Ensure Node.js version compatibility
4. **Auth Issues**: Verify cookie domain settings in backend

### Debug Commands
```bash
# Test build locally
npm run build
npm start

# Check environment variables
echo $NEXT_PUBLIC_API_URL

# Test API connectivity
curl https://api.call-cat.com/health
```

## Summary

**Immediate Actions Required:**
1. **Backend CORS Update** - Add Vercel domains to allowed origins
2. **Deploy to Vercel** - Use CLI or dashboard method above
3. **Test Functionality** - Verify auth and API calls work

**Optional Improvements:**
- Custom domain setup
- Enhanced security headers
- Error monitoring (Sentry, etc.)

The frontend is already well-architected for deployment with proper environment variable usage and production-ready API configuration.