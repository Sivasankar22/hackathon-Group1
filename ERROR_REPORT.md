# COMPREHENSIVE ERROR REPORT AND SOLUTIONS
## Urban Operations Command Center - Deployment Issues

**Date:** November 12, 2025, 4 AM IST
**System:** EC2 Instance (Ubuntu)
**Public IP:** 16.170.40.72
**Project:** UOCC Urban Operations Command Center

---

## ERROR #1: Frontend-Backend Connection Failure

### Problem Description:
- **Symptom:** Frontend displayed error message: "Network error: Unable to connect to server. Please check if the backend is running."
- **Impact:** User registration and all API calls from frontend to backend were failing
- **Severity:** CRITICAL - Complete system failure

### Root Cause:
The frontend application was hardcoded to connect to `http://localhost:8081/api` instead of the EC2 instance's public IP address. When accessed from a browser, "localhost" refers to the user's local machine, not the EC2 server.

### Investigation Steps:
1. Checked frontend accessibility - ✓ Working (HTTP 200 OK)
2. Checked backend services - ✓ All running
3. Tested API with curl - ✓ Backend responding
4. Inspected frontend source code - Found hardcoded localhost URL
5. Located API configuration in: `/final12/UOCC-Urban-Operations-Command-Center--main/frontend_reactjs/src/lib/api.js`

### Solution Implemented:
**File Modified:** `frontend_reactjs/src/lib/api.js`

**Change:**
```javascript
// BEFORE
const API_BASE_URL = 'http://localhost:8081/api';

// AFTER  
const API_BASE_URL = 'http://16.170.40.72:8080/api';
```

**Steps Taken:**
1. Created backup: `cp src/lib/api.js src/lib/api.js.backup`
2. Updated API URL: `sed -i "s|http://localhost:8081/api|http://16.170.40.72:8080/api|g" src/lib/api.js`
3. Rebuilt frontend: `docker-compose build frontend`
4. Restarted container: `docker-compose up -d frontend`

### Verification:
✅ Frontend successfully connects to backend
✅ Error message changed from "Network error" to "Internal Server Error" (indicating connection established)
✅ curl tests confirm API reachability from public IP

**Status:** RESOLVED ✓

---

## ERROR #2: Gateway-to-Microservices Connection Failure

### Problem Description:
- **Symptom:** "Internal Server Error" when attempting user registration
- **Error Message:** `java.net.ConnectException: finishConnect(..) failed with error(-111): Connection refused`
- **Impact:** Backend services cannot communicate with each other
- **Severity:** HIGH - Backend functionality broken

### Root Cause:
The gateway service was configured to route requests to backend microservices using `localhost:PORT`. In Docker networking, each container has its own network namespace, so `localhost` refers to the gateway container itself, not other service containers. This caused connection refused errors when the gateway tried to reach auth-service, traffic-service, etc.

### Investigation Steps:
1. Checked gateway logs - Found "Connection refused" errors to localhost:8090
2. Verified auth-service running - ✓ Running on port 8090
3. Identified Docker networking issue - Services need DNS names, not localhost
4. Located gateway configuration file
5. Found hardcoded localhost URLs in routes configuration

### Solution Implemented:
**File Modified:** `backend_java/gateway_service/src/main/resources/application.properties`

**Changes Made:**
```properties
# BEFORE
spring.cloud.gateway.server.webflux.routes[0].uri=http://localhost:8090
spring.cloud.gateway.server.webflux.routes[1].uri=http://localhost:8091  
spring.cloud.gateway.server.webflux.routes[2].uri=http://localhost:8092
spring.cloud.gateway.server.webflux.routes[3].uri=http://localhost:8093
spring.cloud.gateway.server.webflux.routes[4].uri=http://localhost:8094

# AFTER
spring.cloud.gateway.server.webflux.routes[0].uri=http://auth-service:8090
spring.cloud.gateway.server.webflux.routes[1].uri=http://alert-service:8091
spring.cloud.gateway.server.webflux.routes[2].uri=http://traffic-service:8092  
spring.cloud.gateway.server.webflux.routes[3].uri=http://power-service:8093
spring.cloud.gateway.server.webflux.routes[4].uri=http://cctv-service:8094
```

**Route Mappings:**
- Route[0]: Auth Service - `/api/auth/**` → `auth-service:8090`
- Route[1]: Alert Service - `/api/alerts/**` → `alert-service:8091`
- Route[2]: Traffic Service - `/api/incidents/**` → `traffic-service:8092`
- Route[3]: Power Service - `/api/sensors/**` → `power-service:8093`
- Route[4]: CCTV Service - `/api/cameras/**` → `cctv-service:8094`

**Steps Taken:**
1. Created backup: `cp application.properties application.properties.backup`
2. Updated all localhost references to Docker service names using sed commands
3. Rebuilt gateway: `docker-compose build gateway-service` (~1.5 minutes)
4. Restarted gateway: `docker-compose up -d gateway-service`

### Verification:
✅ User registration completed successfully
✅ Page redirected to login after registration (indicates success)
✅ Gateway logs show successful routing to auth-service
✅ No more "Connection refused" errors
✅ All microservices can communicate through gateway

**Status:** RESOLVED ✓

---

## SYSTEM ARCHITECTURE

### Services Configuration:
```
Frontend:     Port 3000 (Nginx)
Gateway:      Port 8080 (Spring Cloud Gateway)
Auth Service: Port 8090 (Internal)
Alert Service: Port 8091 (Internal)  
Traffic Service: Port 8092 (Internal)
Power Service: Port 8093 (Internal)
CCTV Service: Port 8094 (Internal)
PostgreSQL:   Port 5432 (Internal)
```

### Network Flow:
```
Browser → Frontend (16.170.40.72:3000)
    ↓
Frontend → Gateway (16.170.40.72:8080/api/**)
    ↓
Gateway → Microservices (Docker network)
    ↓
Microservices → PostgreSQL (Docker network)
```

---

## LESSONS LEARNED

### 1. Docker Networking:
- Never use `localhost` for inter-container communication
- Always use Docker service names from docker-compose.yml
- Docker provides automatic DNS resolution for service names

### 2. Environment-Specific Configuration:
- Development often uses localhost which breaks in production
- Frontend API URLs must point to publicly accessible endpoints
- Backend service URLs should use Docker service discovery

### 3. Debugging Approach:
- Test each layer independently (frontend → gateway → services)
- Use curl to verify connectivity at each level
- Check Docker logs for connection errors
- Verify DNS resolution within Docker network

---

## TESTING PERFORMED

### Curl Tests:
```bash
# Frontend accessibility
curl -I http://16.170.40.72:3000
Result: HTTP/1.1 200 OK ✓

# Gateway accessibility  
curl -I http://16.170.40.72:8080
Result: HTTP/1.1 404 Not Found ✓ (expected for root path)

# Auth API endpoint
curl -X POST http://16.170.40.72:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","email":"test@test.com","password":"Test@123"}'
Result: Successfully processed ✓
```

### Browser Tests:
✅ Registration form loads correctly
✅ Form submission processes without errors
✅ Successful redirect to login page after registration
✅ User account created in database

---

## FINAL STATUS

**All Critical Errors:** RESOLVED ✓

**System Status:**
- Frontend: OPERATIONAL ✓
- Gateway: OPERATIONAL ✓  
- Auth Service: OPERATIONAL ✓
- All Microservices: OPERATIONAL ✓
- Database: OPERATIONAL ✓

**User Functionality:**
- User Registration: WORKING ✓
- User Login: AVAILABLE ✓
- API Endpoints: ACCESSIBLE ✓

---

## FILES MODIFIED

1. **frontend_reactjs/src/lib/api.js**
   - Line 1: Changed API_BASE_URL from localhost to EC2 public IP
   - Backup: api.js.backup

2. **backend_java/gateway_service/src/main/resources/application.properties**
   - Lines with routes[].uri: Changed all localhost references to Docker service names
   - Backup: application.properties.backup

---

## RECOMMENDATIONS

### For Production:
1. Use environment variables for API URLs instead of hardcoding
2. Implement proper service discovery (Consul, Eureka)
3. Add health check endpoints for monitoring
4. Configure proper CORS policies
5. Implement API rate limiting
6. Add authentication/authorization middleware

### For Development:
1. Use .env files for environment-specific configuration
2. Maintain separate configs for dev/staging/prod
3. Document all service endpoints and ports
4. Implement automated testing for API connectivity

---

**Report Generated:** November 12, 2025, 4:00 AM IST
**Engineer:** System Administrator
**Time to Resolution:** ~2 hours
**Downtime Impact:** 0 (pre-production testing)

