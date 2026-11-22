# GitHub Actions APK Signing Guide

## Quick Setup for Signed Release APKs

### 1. Generate Keystore (Run Once Locally)
```bash
# Windows PowerShell
keytool -genkeypair -v -keystore iptv-release.keystore -alias iptv-key -keyalg RSA -keysize 2048 -validity 10000 -storepass YOUR_STORE_PASSWORD -keypass YOUR_KEY_PASSWORD -dname "CN=Your Name, OU=IPTV, O=YourOrg, L=City, S=State, C=US"

# Linux/Mac
keytool -genkeypair -v -keystore iptv-release.keystore -alias iptv-key -keyalg RSA -keysize 2048 -validity 10000 -storepass YOUR_STORE_PASSWORD -keypass YOUR_KEY_PASSWORD -dname "CN=Your Name, OU=IPTV, O=YourOrg, L=City, S=State, C=US"
```

### 2. Add Secrets to GitHub Repository
Go to: **Repository → Settings → Secrets and variables → Actions → New repository secret**

| Secret Name | Value |
|-------------|-------|
| `KEYSTORE_BASE64` | Base64 encoded keystore file |
| `KEYSTORE_PASSWORD` | Store password from step 1 |
| `KEY_ALIAS` | `iptv-key` (or your alias) |
| `KEY_PASSWORD` | Key password from step 1 |

**To get KEYSTORE_BASE64:**
```bash
# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("iptv-release.keystore")) | Set-Clipboard

# Linux/Mac
base64 -i iptv-release.keystore | pbcopy
```

### 3. Update build.gradle.kts for Signing
Add to `app/build.gradle.kts` in `android` block:

```kotlin
android {
    // ... existing config ...

    signingConfigs {
        create("release") {
            val keystoreBase64 = System.getenv("KEYSTORE_BASE64") ?: ""
            val storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            val keyAlias = System.getenv("KEY_ALIAS") ?: ""
            val keyPassword = System.getenv("KEY_PASSWORD") ?: ""

            if (keystoreBase64.isNotEmpty()) {
                // Decode keystore from base64
                val keystoreBytes = android.util.Base64.decode(keystoreBase64, android.util.Base64.DEFAULT)
                val keystoreFile = file("$buildDir/keystore.jks")
                keystoreFile.writeBytes(keystoreBytes)

                storeFile = keystoreFile
                storePassword = storePassword
                keyAlias = keyAlias
                keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 4. Updated Release Workflow (`.github/workflows/release.yml`)

```yaml
name: Release Build

on:
  push:
    tags: ['v*']
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'
      - uses: android-actions/setup-android@v3
        with:
          api-level: 34
      - name: Build Release APK
        run: ./gradlew assembleRelease --no-daemon
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      - name: Upload Signed APK
        uses: actions/upload-artifact@v4
        with:
          name: app-release-signed
          path: app/build/outputs/apk/release/app-release.apk
      - name: Create Release
        uses: softprops/action-gh-release@v2
        with:
          files: app/build/outputs/apk/release/app-release.apk
```

---

## Without Keystore (Current Setup)

The current workflow builds:
- **Debug APK** - Installable directly (debuggable)
- **Release APK (unsigned)** - For testing only, won't install on most devices

**To install unsigned release APK:**
```bash
# Allow unknown sources + disable signature verification (root required)
adb install -t app-release-unsigned.apk
```

---

## Alternative: Build Locally Without Android Studio

### Using Command Line Only
```bash
# 1. Clone
git clone https://github.com/YOUR_USERNAME/IPTVApp.git
cd IPTVApp

# 2. Build (needs JDK 17 + Android SDK)
./gradlew assembleDebug

# 3. APK location
ls app/build/outputs/apk/debug/
```

### Using Docker (Fully Isolated)
```dockerfile
# Dockerfile
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew assembleDebug --no-daemon

FROM alpine:latest
COPY --from=builder /app/app/build/outputs/apk/debug/app-debug.apk /app-debug.apk
```

```bash
docker build -t iptv-build .
docker run --rm -v $(pwd)/output:/out iptv-build cp /app-debug.apk /out/
```

---

## GitHub Codespaces (Free Cloud Build)

1. Open repo on GitHub → **Code → Codespaces → Create codespace on main**
2. Wait for container to start
3. Run in terminal:
   ```bash
   ./gradlew assembleDebug
   ```
4. Download APK from file explorer

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| `SDK location not found` | Add `local.properties` with `sdk.dir` or use `android-actions/setup-android` |
| `Keystore not found` | Ensure secrets are set correctly in GitHub |
| `Out of memory` | Add `org.gradle.jvmargs=-Xmx4g` to `gradle.properties` |
| `License not accepted` | Run `yes | sdkmanager --licenses` locally, or use setup-android action |

---

## Security Notes

✅ **DO:**
- Store keystore in GitHub Secrets (encrypted)
- Use different keystores for debug/release
- Rotate keys periodically

❌ **DON'T:**
- Commit keystore files to git
- Hardcode passwords in build.gradle.kts
- Share release keystore publicly