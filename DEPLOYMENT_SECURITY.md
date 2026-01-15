# Deployment Security Configuration

## GitHub Secrets Setup

Add these secrets to your GitHub repository:

### Required Secrets
- `SERVER_URL`: Your production server URL (e.g., `https://your-domain.com` or `http://your-ip:8080`)
- `RESTART_PASSWORD`: The same password as `server.restart.password` in application.properties

### How to Add Secrets
1. Go to your GitHub repository
2. Navigate to: Settings > Secrets and variables > Actions
3. Click "New repository secret"
4. Add each secret with the corresponding values

## Security Recommendations

### 1. Use HTTPS (Highly Recommended)
- Set up SSL/TLS certificate (Let's Encrypt via Certbot)
- Configure reverse proxy (Nginx/Caddy) in front of your Spring Boot app
- Example Nginx config:
  ```nginx
  server {
      listen 443 ssl;
      server_name your-domain.com;
      
      ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
      ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
      
      location /api/system/ {
          proxy_pass http://localhost:8080;
          proxy_set_header Host $host;
          proxy_set_header X-Real-IP $remote_addr;
      }
  }
  ```

### 2. Firewall Rules
```bash
# Only allow specific GitHub Actions IPs (optional but very secure)
# Get GitHub Actions IP ranges: https://api.github.com/meta

# Basic firewall setup
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 443/tcp   # HTTPS
sudo ufw deny 8080/tcp   # Block direct access to Spring Boot
sudo ufw enable
```

### 3. Strong Password
Update `application.properties` with a strong password:
```properties
server.restart.password=YOUR_VERY_STRONG_RANDOM_PASSWORD_HERE
```
Generate one: `openssl rand -base64 32`

### 4. IP Whitelisting (Optional)
Add to SystemController.java:
```java
@Value("${deployment.allowed.ips:0.0.0.0/0}")
private String allowedIps;

@PostMapping("/restart")
public ResponseEntity<Map<String, Object>> restart(
        @RequestBody Map<String, String> payload,
        HttpServletRequest request) {
    
    String clientIp = request.getRemoteAddr();
    if (!isIpAllowed(clientIp)) {
        logger.warning("Restart attempt from unauthorized IP: " + clientIp);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    // ... rest of code
}
```

### 5. Rate Limiting
Add Spring Boot Rate Limiter (bucket4j) to prevent brute force:
```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

### 6. Monitoring & Alerts
- Set up Discord/Slack notifications for deployment events
- Monitor unauthorized restart attempts
- Log all restart requests with IP addresses

### 7. Least Privilege
- Run service as non-root user (`pzuser`)
- Limit file permissions: `chmod 600 application.properties`

## SystemD Service Configuration

Create `/etc/systemd/system/pzmanager.service`:

```ini
[Unit]
Description=Project Zomboid Manager
After=network.target

[Service]
Type=simple
User=pzuser
Group=pzuser
WorkingDirectory=/home/pzuser/pzmanager
ExecStart=/usr/bin/java -Dspring.profiles.active=prod -jar /home/pzuser/pzmanager/target/manager.jar
Restart=always
RestartSec=10

# Security Hardening
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=read-only
ReadWritePaths=/home/pzuser/pzmanager /home/pzuser/Zomboid

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable pzmanager
sudo systemctl start pzmanager
```

## Testing the Setup

### 1. Test locally first:
```bash
curl -X POST http://localhost:8080/api/system/restart \
  -H "Content-Type: application/json" \
  -d '{"password": "your-password"}'
```

### 2. Test health endpoint:
```bash
curl http://localhost:8080/api/system/health
```

### 3. Test from GitHub Actions (manual workflow run)
- Go to Actions tab > Deploy to Production > Run workflow

## Rollback Plan
If deployment fails:
```bash
# SSH into server
cd /home/pzuser/pzmanager
git log --oneline -5
git reset --hard <previous-commit-hash>
sudo systemctl restart pzmanager
```
