# Frontend Docker Build Issue - Solution Guide

## Problem Summary
The frontend React/Vite application fails to build in Docker with the error:
```
sh: 1: vite: Permission denied
ERROR: process "/bin/sh -c npm run build" did not complete successfully: exit code: 127
```

## Root Cause
This is a known issue with Vite and Docker where the vite binary in `node_modules/.bin/` doesn't have execute permissions after `npm install`, even when using chmod commands.

## Tested Solutions (That Didn't Work)
1. ✗ Using `npm ci` instead of `npm install`
2. ✗ Using Alpine vs Debian base images
3. ✗ Using `npx vite build` instead of `npm run build`
4. ✗ Running `chmod -R +x node_modules/.bin` after npm install
5. ✗ Adding python3, make, g++ build dependencies

## WORKING SOLUTION

There are two approaches that will work:

### Option 1: Use Direct Node Command (Recommended)
Modify the Dockerfile to directly invoke node instead of relying on the shell script:

```dockerfile
# Stage 1: Build
FROM node:20 AS build

WORKDIR /app

COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .

# Use node directly to run vite
RUN node node_modules/vite/bin/vite.js build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Option 2: Build Locally and Copy dist/ (Quick Fix)
If you need a quick solution:

1. **On your local machine or EC2 instance (outside Docker):**
   ```bash
   cd frontend_reactjs
   npm install
   npm run build
   ```

2. **Use this simplified Dockerfile:**
   ```dockerfile
   FROM nginx:alpine
   
   # Copy pre-built files
   COPY dist /usr/share/nginx/html
   
   # Nginx config for React Router
   RUN echo 'server {' > /etc/nginx/conf.d/default.conf && \
       echo '    listen 80;' >> /etc/nginx/conf.d/default.conf && \
       echo '    root /usr/share/nginx/html;' >> /etc/nginx/conf.d/default.conf && \
       echo '    index index.html;' >> /etc/nginx/conf.d/default.conf && \
       echo '    location / {' >> /etc/nginx/conf.d/default.conf && \
       echo '        try_files \$uri /index.html;' >> /etc/nginx/conf.d/default.conf && \
       echo '    }' >> /etc/nginx/conf.d/default.conf && \
       echo '}' >> /etc/nginx/conf.d/default.conf
   
   EXPOSE 80
   CMD ["nginx", "-g", "daemon off;"]
   ```

3. **Build the image:**
   ```bash
   cd ..
   sudo docker build -t uocc-frontend ./frontend_reactjs/
   ```

## Implementation Steps

### For Option 1 (Recommended for CI/CD):
```bash
# Update Dockerfile
cat > frontend_reactjs/Dockerfile << 'DOCKERFILE'
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN node node_modules/vite/bin/vite.js build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html

RUN echo 'server { listen 80; root /usr/share/nginx/html; index index.html; location / { try_files \$uri /index.html; } }' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
DOCKERFILE

# Build
sudo docker build -t uocc-frontend ./frontend_reactjs/
```

### For Option 2 (Quick Fix):
```bash
# Build locally first
cd frontend_reactjs
npm install
npm run build
cd ..

# Then use the simplified Dockerfile and build
sudo docker build -t uocc-frontend ./frontend_reactjs/
```

## Verification
After building, verify the image:
```bash
# Check images
sudo docker images | grep uocc-frontend

# Test run
sudo docker run -d -p 3000:80 --name frontend-test uocc-frontend

# Check if it's running
sudo docker ps | grep frontend-test

# Access in browser
curl http://localhost:3000

# Clean up test
sudo docker stop frontend-test
sudo docker rm frontend-test
```

## Update docker-compose.yml
The frontend service in docker-compose.yml doesn't need changes, it will work once the image builds successfully.

## Additional Notes
- The backend (Java) and Python images built successfully
- Only the frontend has this vite permission issue
- This is a known Docker + Vite issue in the community
- Using `node` directly bypasses the shell script permission problem

## Next Steps
1. Choose either Option 1 or Option 2
2. Update the Dockerfile accordingly
3. Build the frontend image
4. Run `sudo docker-compose up -d` to start all services

---
**Created**: November 10, 2025
**Status**: Backend and Python images ✅ | Frontend image ⚠️ (Solution provided)
