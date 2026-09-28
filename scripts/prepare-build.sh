#!/usr/bin/env bash
set -e

echo "=== DevToggle: Preparing Build Environment ==="
pwd
ls -la

# 1. Search for subfolder containing the project if not in root
if [ ! -f "settings.gradle.kts" ] && [ ! -f "settings.gradle" ]; then
  SETTINGS_FILE=$(find . -mindepth 2 -name "settings.gradle.kts" -o -name "settings.gradle" 2>/dev/null | head -n 1)
  if [ -n "$SETTINGS_FILE" ]; then
    PROJECT_DIR=$(dirname "$SETTINGS_FILE")
    echo "Found project in $PROJECT_DIR. Moving files to root workspace..."
    shopt -s dotglob
    mv "$PROJECT_DIR"/* . 2>/dev/null || true
  fi
fi

# 2. Ensure settings.gradle.kts exists
if [ ! -f "settings.gradle.kts" ] && [ ! -f "settings.gradle" ]; then
  echo "settings.gradle.kts is missing. Creating standard settings.gradle.kts..."
  cat << 'EOF' > settings.gradle.kts
pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "DevToggle"

include(":app")
EOF
fi

# 3. Ensure root build.gradle.kts uses Gradle 9+ compatible explicit IDs
if [ ! -f "build.gradle.kts" ] && [ ! -f "build.gradle" ]; then
  echo "build.gradle.kts is missing. Creating root build.gradle.kts..."
  cat << 'EOF' > build.gradle.kts
plugins {
  id("com.android.application") version "9.1.1" apply false
  id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
  id("com.google.devtools.ksp") version "2.3.5" apply false
  id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
  id("com.google.gms.google-services") version "4.5.0" apply false
}
EOF
fi

# 4. Ensure gradle.properties exists
if [ ! -f "gradle.properties" ]; then
  echo "gradle.properties is missing. Creating standard gradle.properties..."
  cat << 'EOF' > gradle.properties
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
org.gradle.parallel=true
kotlin.code.style=official
android.nonTransitiveRClass=true
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.workers.max=4
kotlin.compiler.execution.strategy=in-process
googleServices.missing.passthrough=true
EOF
fi

# 5. Ensure .env exists to satisfy Secrets Gradle Plugin
if [ ! -f ".env" ]; then
  if [ -f ".env.example" ]; then
    cp .env.example .env
  else
    touch .env
  fi
fi

# 6. Ensure debug.keystore exists for debug signing
if [ ! -f "debug.keystore" ]; then
  if [ -f "debug.keystore.base64" ]; then
    echo "Restoring debug.keystore from base64..."
    base64 -d debug.keystore.base64 > debug.keystore || true
  fi
  if [ ! -f "debug.keystore" ]; then
    echo "Generating fallback debug.keystore..."
    keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" 2>/dev/null || true
  fi
fi

# 7. Ensure gradlew is executable
chmod +x gradlew 2>/dev/null || true

echo "=== Build Environment Ready ==="
ls -la
