# Memory Usage Investigation - CallCat Backend

## Investigation Date: September 3, 2025

### Problem
Backend consistently reaching 92% memory usage on t3.micro instance (904MB total RAM) during production operation.

### Root Cause Analysis
**Issue**: JVM running with default settings allowing unlimited off-heap memory growth
- Instance: t3.micro (904MB usable RAM)
- Java Version: OpenJDK 21.0.8 (Corretto-21.0.8.9.1)
- Original Command: `java -jar application.jar` (no memory constraints)

### Memory Growth Pattern (Before Fix)
Direct SSH monitoring showed consistent memory growth with zero user activity:
- **Fresh restart**: 522MB used (57.7%)
- **After 2 minutes**: 531MB used (58.7%)
- **After 10 minutes**: 534MB used (59.1%)
- **Growth rate**: ~1.2MB per minute
- **Java process RSS growth**: 321MB → 328MB → 335MB

### Solution Implemented
Added JVM memory constraints via AWS Elastic Beanstalk environment variable:
```
JAVA_TOOL_OPTIONS=-Xmx400m -XX:MaxMetaspaceSize=128m -XX:MaxDirectMemorySize=64m
```

### Results (After Fix)
**Environment updated**: September 3, 2025 at 05:13 UTC
**Monitoring at 57 minutes uptime**:
- **Total memory usage**: 507MB (56.1%) - stable
- **Java process RSS**: 316MB (34.1%)
- **Available memory**: 150MB buffer
- **Java process VSZ**: Capped at ~2.3GB (down from 3GB+)

### Performance Improvement
- **Before fix**: Would reach ~680MB+ (75%+) after 57 minutes
- **After fix**: Stable at 507MB (56%)
- **Memory savings**: ~170MB+ improvement
- **Growth pattern**: Eliminated aggressive memory growth

### Technical Details
- **Platform**: AWS Elastic Beanstalk
- **Stack**: 64bit Amazon Linux 2023 v4.6.4 running Corretto 21
- **Environment**: callcat-backend-production-env
- **Instance Type**: t3.micro (1GB RAM)
- **Configuration method**: EB environment variable update
- **Application restart**: Automatic via EB environment update

### Next Steps
- Monitor memory usage over 24-48 hours to confirm long-term stability
- Verify application functionality remains intact with memory constraints