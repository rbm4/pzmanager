#!/bin/bash

REPO="rbm4/pzmanager"
INSTALL_DIR="/home/pzuser/pzmanager"
JAR_NAME="pzmanager.jar"
ROLLBACK_JAR="rollback.jar"
VERSION_FILE="current-version.txt"
STARTUP_TIMEOUT=30

cd "$INSTALL_DIR"

# Pull latest config/scripts from the repo
git pull

# ── Resolve versions ──────────────────────────────────────────────

LATEST_VERSION=$(gh release view --repo "$REPO" --json tagName -q .tagName 2>/dev/null)

if [ -z "$LATEST_VERSION" ]; then
  echo "⚠️ Could not determine latest release version"
  if [ -f "$JAR_NAME" ]; then
    echo "Running existing JAR..."
    java -Dspring.profiles.active=prod -jar "$JAR_NAME"
  fi
  exit 1
fi

CURRENT_VERSION=""
if [ -f "$VERSION_FILE" ]; then
  CURRENT_VERSION=$(cat "$VERSION_FILE")
fi

echo "Latest release : $LATEST_VERSION"
echo "Current version: ${CURRENT_VERSION:-none}"

# ── Skip download if already up-to-date ───────────────────────────

if [ "$LATEST_VERSION" = "$CURRENT_VERSION" ] && [ -f "$JAR_NAME" ]; then
  echo "✅ Already running the latest version ($CURRENT_VERSION). Skipping download."
  java -Dspring.profiles.active=prod -jar "$JAR_NAME"
  exit $?
fi

echo "New version available: $LATEST_VERSION"

# ── Backup current JAR for rollback ───────────────────────────────

if [ -f "$JAR_NAME" ]; then
  echo "Backing up current JAR as $ROLLBACK_JAR..."
  cp "$JAR_NAME" "$ROLLBACK_JAR"
fi

# ── Download new JAR ──────────────────────────────────────────────

echo "Downloading release $LATEST_VERSION..."
rm -f "$JAR_NAME"
gh release download "$LATEST_VERSION" --repo "$REPO" --pattern "*.jar" --output "$JAR_NAME" --clobber

if [ ! -f "$JAR_NAME" ]; then
  echo "❌ Failed to download JAR from release $LATEST_VERSION"
  if [ -f "$ROLLBACK_JAR" ]; then
    echo "⏪ Rolling back to previous version..."
    mv "$ROLLBACK_JAR" "$JAR_NAME"
    java -Dspring.profiles.active=prod -jar "$JAR_NAME"
  fi
  exit 1
fi

echo "✅ Downloaded $LATEST_VERSION successfully"

# ── Start new version and verify it survives startup ──────────────

java -Dspring.profiles.active=prod -jar "$JAR_NAME" &
APP_PID=$!

echo "Waiting ${STARTUP_TIMEOUT}s for application to start (PID $APP_PID)..."
sleep "$STARTUP_TIMEOUT"

if kill -0 "$APP_PID" 2>/dev/null; then
  # Process is still alive — startup succeeded
  echo "✅ Application started successfully with version $LATEST_VERSION"
  echo "$LATEST_VERSION" > "$VERSION_FILE"
  rm -f "$ROLLBACK_JAR"
  # Re-attach to the process so systemd tracks it correctly
  wait "$APP_PID"
else
  # Process crashed during startup — rollback
  echo "❌ Application crashed during startup with version $LATEST_VERSION"

  if [ -f "$ROLLBACK_JAR" ]; then
    echo "⏪ Rolling back to previous version (${CURRENT_VERSION:-unknown})..."
    rm -f "$JAR_NAME"
    mv "$ROLLBACK_JAR" "$JAR_NAME"
    # Restore previous version marker
    if [ -n "$CURRENT_VERSION" ]; then
      echo "$CURRENT_VERSION" > "$VERSION_FILE"
    fi
    java -Dspring.profiles.active=prod -jar "$JAR_NAME"
  else
    echo "❌ No rollback JAR available. Cannot recover."
    exit 1
  fi
fi