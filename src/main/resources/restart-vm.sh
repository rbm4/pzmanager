#!/bin/bash

# Restart VM Script
# This script will restart the virtual machine

echo "Initiating VM restart..."

# Add a small delay to ensure the application can log this message
sleep 5

# Restart the VM (requires appropriate sudo permissions)
# Option 1: Using systemctl (systemd)
sudo systemctl reboot

# Option 2: Using shutdown command (alternative)
# sudo shutdown -r now

# Option 3: Using reboot command (alternative)
# sudo reboot

echo "Restart command issued"
