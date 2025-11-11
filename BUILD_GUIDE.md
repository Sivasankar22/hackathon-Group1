# Docker Build Guide for UOCC Urban Operations Command Center

## Overview
This guide will help you build and run all Docker images for the Urban Operations Command Center application.

## Project Structure
```
.
├── docker-compose.yml           # Main orchestration file
├── backend_java/
│   ├── Dockerfile               # Java microservices
│   ├── gateway_service/
│   ├── auth_service/
│   ├── traffic_service/
│   ├── cctv_service/
│   ├── power_service/
│   └── alert_service/
├── frontend_reactjs/
│   └── Dockerfile               # React frontend
└── python/urban-operations-python/
    └── Dockerfile               # Python service
```

## Services and Ports
- **Frontend**: Port 3000 (React + Nginx)
- **Gateway Service**: Port 8080 (Java/Spring Boot)
- **Auth Service**: Port 8081 (Java/Spring Boot)
- **Traffic Service**: Port 8082 (Java/Spring Boot)
- **CCTV Service**: Port 8083 (Java/Spring Boot)
- **Power Service**: Port 8084 (Java/Spring Boot)
- **Alert Service**: Port 8085 (Java/Spring Boot)
- **Python Service**: Port 8000 (FastAPI/Python)
- **PostgreSQL Database**: Port 5432

## Prerequisites
- Docker installed ✅
- Docker Compose installed ✅
- Minimum 8GB RAM recommended
- 20GB free disk space

## Building Docker Images

### Option 1: Build All Services at Once (Recommended)
```bash
# Build all images using docker-compose
sudo docker-compose build

# Or build with no cache (fresh build)
sudo docker-compose build --no-cache
```

### Option 2: Build Individual Services

#### Build Frontend Only
```bash
sudo docker build -t uocc-frontend ./frontend_reactjs/
```

#### Build Backend Java Services
```bash
sudo docker build -t uocc-backend ./backend_java/
```

#### Build Python Service
```bash
sudo docker build -t uocc-python ./python/urban-operations-python/
```

## Running the Application

### Start All Services
```bash
# Start all services in detached mode
sudo docker-compose up -d

# View logs
sudo docker-compose logs -f

# View logs for specific service
sudo docker-compose logs -f gateway-service
```

### Start Specific Services
```bash
# Start only database and gateway
sudo docker-compose up -d postgres gateway-service

# Start frontend only
sudo docker-compose up -d frontend
```

## Managing Containers

### Check Running Containers
```bash
sudo docker-compose ps

# Or use docker command
sudo docker ps
```

### Stop Services
```bash
# Stop all services
sudo docker-compose down

# Stop and remove volumes (WARNING: This deletes database data)
sudo docker-compose down -v
```

### Restart Services
```bash
# Restart all services
sudo docker-compose restart

# Restart specific service
sudo docker-compose restart gateway-service
```

### View Container Logs
```bash
# All services
sudo docker-compose logs

# Specific service with follow
sudo docker-compose logs -f auth-service

# Last 100 lines
sudo docker-compose logs --tail=100 postgres
```

## Troubleshooting

### Check Service Health
```bash
# Check if database is ready
sudo docker exec uocc-postgres pg_isready -U postgres

# Check Java service logs
sudo docker logs gateway-service
```

### Rebuild After Code Changes
```bash
# Rebuild and restart specific service
sudo docker-compose up -d --build gateway-service

# Rebuild all services
sudo docker-compose up -d --build
```

### Access Container Shell
```bash
# Access PostgreSQL
sudo docker exec -it uocc-postgres psql -U postgres -d urbanops

# Access backend container
sudo docker exec -it gateway-service sh

# Access frontend container
sudo docker exec -it frontend sh
```

### Clean Up Docker Resources
```bash
# Remove stopped containers
sudo docker container prune

# Remove unused images
sudo docker image prune

# Remove all unused resources (BE CAREFUL!)
sudo docker system prune -a
```

## Database Management

### Create Database Backup
```bash
sudo docker exec uocc-postgres pg_dump -U postgres urbanops > backup.sql
```

### Restore Database
```bash
cat backup.sql | sudo docker exec -i uocc-postgres psql -U postgres urbanops
```

### Access Database
```bash
sudo docker exec -it uocc-postgres psql -U postgres -d urbanops
```

## Performance Monitoring

### Check Resource Usage
```bash
# Real-time stats for all containers
sudo docker stats

# Stats for specific container
sudo docker stats gateway-service
```

### Check Disk Usage
```bash
sudo docker system df
```

## Network Configuration

### Inspect Network
```bash
# List networks
sudo docker network ls

# Inspect UOCC network
sudo docker network inspect uocc-urban-operations-command-center--main_uocc-network
```

## Quick Commands Reference

```bash
# Build everything
sudo docker-compose build

# Start everything
sudo docker-compose up -d

# Check status
sudo docker-compose ps

# View all logs
sudo docker-compose logs -f

# Stop everything
sudo docker-compose down

# Restart a service
sudo docker-compose restart <service-name>

# Rebuild and restart
sudo docker-compose up -d --build
```

## Common Issues and Solutions

### Issue: Port Already in Use
**Solution**: Change port mapping in docker-compose.yml or stop conflicting service
```bash
# Find process using port
sudo netstat -tulpn | grep :8080

# Kill process
sudo kill -9 <PID>
```

### Issue: Out of Memory
**Solution**: Increase Docker memory limit or reduce number of running services

### Issue: Build Fails for Java Services
**Solution**: Check if Maven can download dependencies
```bash
# Build with verbose output
sudo docker-compose build --progress=plain backend_java
```

### Issue: Frontend Build Fails
**Solution**: Check Node.js version compatibility
```bash
# Rebuild with no cache
sudo docker-compose build --no-cache frontend
```

## Production Considerations

1. **Environment Variables**: Use .env file for sensitive data
2. **Volumes**: Backup PostgreSQL data regularly
3. **Logging**: Configure log rotation
4. **Security**: Change default passwords
5. **Monitoring**: Set up health checks and monitoring tools

## Next Steps

After building and running:
1. Access frontend at: http://localhost:3000
2. API Gateway at: http://localhost:8080
3. Check individual service health endpoints
4. Configure environment-specific settings
5. Set up monitoring and logging

---
**Last Updated**: 2025
**Version**: 1.0.0
