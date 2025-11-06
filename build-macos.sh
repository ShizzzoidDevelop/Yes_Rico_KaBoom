#!/bin/bash

# Build script for macOS .app bundle
# Creates native macOS application with Retina support

set -e

echo "Building Yes Rico KaBoom for macOS..."

# Build the project
./gradlew desktop:build

# Create .app bundle structure
APP_NAME="Yes Rico KaBoom"
APP_DIR="dist/${APP_NAME}.app"
CONTENTS_DIR="${APP_DIR}/Contents"
MACOS_DIR="${CONTENTS_DIR}/MacOS"
RESOURCES_DIR="${CONTENTS_DIR}/Resources"

# Clean previous build
rm -rf dist/

# Create directory structure
mkdir -p "${MACOS_DIR}"
mkdir -p "${RESOURCES_DIR}"

# Copy Info.plist
cat > "${CONTENTS_DIR}/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>YesRicoKaBoom</string>
    <key>CFBundleIdentifier</key>
    <string>com.yesricokaboom.minesweeper</string>
    <key>CFBundleName</key>
    <string>Yes Rico KaBoom</string>
    <key>CFBundleVersion</key>
    <string>1.0.0</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.15</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>NSRequiresAquaSystemAppearance</key>
    <false/>
</dict>
</plist>
EOF

# Copy JAR and dependencies
cp desktop/build/libs/desktop-*.jar "${RESOURCES_DIR}/game.jar"
cp -r desktop/build/libs/lib/* "${RESOURCES_DIR}/"

# Create launcher script
cat > "${MACOS_DIR}/YesRicoKaBoom" << 'EOF'
#!/bin/bash

# Get the directory where the script is located
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
APP_DIR="$(dirname "$DIR")"
RESOURCES_DIR="$APP_DIR/Resources"

# Set Java options for macOS
export JAVA_OPTS="-XstartOnFirstThread -Djava.awt.headless=true"

# Find Java
if [ -d "/Library/Java/JavaVirtualMachines" ]; then
    JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home -v 11 2>/dev/null || echo "")
    if [ -n "$JAVA_HOME" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
else
    JAVA="java"
fi

# Run the game
cd "$RESOURCES_DIR"
exec "$JAVA" $JAVA_OPTS -cp "game.jar:lib/*" com.yesricokaboom.desktop.DesktopLauncher
EOF

chmod +x "${MACOS_DIR}/YesRicoKaBoom"

# Copy assets
mkdir -p "${RESOURCES_DIR}/assets"
cp -r core/src/main/resources/* "${RESOURCES_DIR}/assets/" 2>/dev/null || true

echo "✅ macOS .app bundle created: ${APP_DIR}"
echo "📱 Retina support enabled"
echo "🚀 Run: open '${APP_DIR}'"

