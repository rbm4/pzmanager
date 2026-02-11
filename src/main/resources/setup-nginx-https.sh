#!/bin/bash

# NGINX + Let's Encrypt HTTPS Setup Script
# For Apocalipse BR Manager Application
# 
# Prerequisites:
# - Run as root or with sudo
# - Domain name pointing to this server's IP
# - Ports 80 and 443 must be accessible from the internet
# - Java application should be running on localhost:8080

set -e  # Exit on error

# Configuration variables
DOMAIN="apocalipse.cloud"
EMAIL="admin@apocalipse.cloud"  # Change this to your email for Let's Encrypt notifications
APP_PORT="8080"
NGINX_SITE_NAME="apocalipsebr"

echo "=========================================="
echo "NGINX + Let's Encrypt Setup for $DOMAIN"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "Please run as root or with sudo"
    exit 1
fi

# Update system packages
echo "[1/7] Updating system packages..."
apt update
apt upgrade -y

# Install Nginx
echo "[2/7] Installing Nginx..."
apt install nginx -y
systemctl start nginx
systemctl enable nginx

# Install Certbot for Let's Encrypt
echo "[3/7] Installing Certbot..."
apt install certbot python3-certbot-nginx -y

# Stop Nginx temporarily for certificate generation
echo "[4/7] Stopping Nginx temporarily..."
systemctl stop nginx

# Generate Let's Encrypt certificate
echo "[5/7] Generating Let's Encrypt SSL certificate..."
echo "This will validate your domain ownership. Make sure $DOMAIN points to this server!"
certbot certonly --standalone -d $DOMAIN --non-interactive --agree-tos --email $EMAIL

# Create Nginx configuration
echo "[6/7] Creating Nginx configuration..."
cat > /etc/nginx/sites-available/$NGINX_SITE_NAME << 'EOF'
# HTTP to HTTPS redirect
server {
    listen 80;
    listen [::]:80;
    server_name apocalipse.cloud;
    
    # Allow Let's Encrypt validation
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }
    
    # Redirect all other traffic to HTTPS
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name apocalipse.cloud;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/apocalipse.cloud/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/apocalipse.cloud/privkey.pem;
    
    # SSL Security Settings (recommended by Mozilla)
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers off;
    
    # Security headers
    add_header Strict-Transport-Security "max-age=63072000" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Logging
    access_log /var/log/nginx/apocalipsebr_access.log;
    error_log /var/log/nginx/apocalipsebr_error.log;
    
    # Proxy settings
    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        
        # Headers for proper proxy
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # WebSocket support (if needed)
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Buffer settings
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
        proxy_busy_buffers_size 8k;
    }
    
    # Increase max upload size if needed
    client_max_body_size 10M;
}
EOF

# Enable the site
echo "Enabling Nginx site configuration..."
ln -sf /etc/nginx/sites-available/$NGINX_SITE_NAME /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default  # Remove default site

# Test Nginx configuration
echo "Testing Nginx configuration..."
nginx -t

# Start Nginx
echo "Starting Nginx..."
systemctl start nginx
systemctl enable nginx

# Setup automatic certificate renewal
echo "[7/7] Setting up automatic SSL certificate renewal..."
cat > /etc/cron.d/certbot-renew << 'EOF'
# Renew Let's Encrypt certificates twice daily and reload Nginx if renewed
0 */12 * * * root certbot renew --quiet --post-hook "systemctl reload nginx"
EOF

# Test renewal process
echo "Testing certificate renewal process..."
certbot renew --dry-run

echo ""
echo "=========================================="
echo "✓ Setup Complete!"
echo "=========================================="
echo ""
echo "Your application should now be accessible at:"
echo "  https://$DOMAIN"
echo ""
echo "Important notes:"
echo "  1. Make sure your Java application is running on localhost:8080"
echo "  2. Certificates will auto-renew every 90 days"
echo "  3. Check Nginx logs at: /var/log/nginx/apocalipsebr_*.log"
echo "  4. To restart Nginx: systemctl restart nginx"
echo "  5. To check Nginx status: systemctl status nginx"
echo ""
echo "Firewall configuration (if using ufw):"
echo "  sudo ufw allow 80/tcp"
echo "  sudo ufw allow 443/tcp"
echo "  sudo ufw reload"
echo ""
echo "Testing commands:"
echo "  curl -I https://$DOMAIN"
echo "  curl http://$DOMAIN  # Should redirect to HTTPS"
echo ""
