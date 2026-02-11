# HTTPS Setup Instructions with Nginx

## Quick Setup Guide

### 1. Prepare the Script

Before running on your Linux server, edit the script to set your email:

```bash
nano setup-nginx-https.sh
# Change: EMAIL="admin@apocalipse.cloud" to your actual email
```

### 2. Transfer and Execute Script on Linux Server

```bash
# Transfer the script to your server
scp setup-nginx-https.sh pzuser@apocalipse.cloud:/tmp/

# SSH into your server
ssh pzuser@apocalipse.cloud

# Make the script executable
chmod +x /tmp/setup-nginx-https.sh

# Run the script as root
sudo /tmp/setup-nginx-https.sh
```

### 3. Deploy Your Application

After nginx is configured, deploy your updated application:

```bash
# Build and deploy using your existing script
./build-and-deploy.ps1
```

### 4. Verify Setup

```bash
# On your server, check if nginx is running
sudo systemctl status nginx

# Check if your Java app is running
ps aux | grep java

# Test HTTPS connection
curl -I https://apocalipse.cloud

# Test HTTP redirect
curl -I http://apocalipse.cloud
```

## Troubleshooting

### Check Nginx Logs
```bash
sudo tail -f /var/log/nginx/apocalipsebr_error.log
sudo tail -f /var/log/nginx/apocalipsebr_access.log
```

### Check Certificate Status
```bash
sudo certbot certificates
```

### Test Nginx Configuration
```bash
sudo nginx -t
```

### Restart Services
```bash
# Restart Nginx
sudo systemctl restart nginx

# Restart your Java application
sudo systemctl restart your-app-service-name
```

### Firewall Configuration
If using UFW:
```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw reload
sudo ufw status
```

## What Changed in Your Application

1. **Base URL**: Changed from `http://apocalipse.cloud:8080` to `https://apocalipse.cloud`
2. **Port**: Application still runs on `8080` (localhost only)
3. **Proxy Headers**: Added configuration to properly handle reverse proxy headers

## How It Works

```
Internet → Nginx (Port 443, HTTPS) → Your Java App (localhost:8080)
```

- Nginx handles SSL/TLS encryption
- Nginx forwards requests to your app on localhost:8080
- Your app doesn't need to handle SSL directly
- Users access via standard HTTPS port (443)

## Certificate Renewal

Certificates auto-renew via cron job. Manual renewal:
```bash
sudo certbot renew
sudo systemctl reload nginx
```

## Security Notes

- Your Java app is only accessible via localhost (not exposed to internet directly)
- Nginx handles all external connections
- SSL certificates are free and auto-renewing
- Security headers are configured in Nginx
