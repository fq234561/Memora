# Memora Release Identity Setup

This document is the release identity checklist for the international Android app.

## Current Decisions

- App name: `Memora`
- Google Play package name: `com.memora.familyphotos`
- Android source namespace: `com.memorial.app`
- Backend Google Play package env: `GOOGLE_PLAY_PACKAGE_NAME=com.memora.familyphotos`

The Android `applicationId` is the public package name used by Google Play, Google OAuth Android clients, and Google Play Billing. The Kotlin source namespace can be migrated later if needed, but it does not block Play release identity.

## Release Signing

Do not commit keystores or passwords.

Generate the release keystore outside the repository, for example:

```powershell
New-Item -ItemType Directory -Force "D:\memora-secrets"
keytool -genkeypair -v `
  -keystore "D:\memora-secrets\memora-release.jks" `
  -alias memora `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000
```

Set environment variables before building a signed release:

```powershell
$env:MEMORA_RELEASE_KEYSTORE="D:\memora-secrets\memora-release.jks"
$env:MEMORA_RELEASE_KEYSTORE_PASSWORD="<store-password>"
$env:MEMORA_RELEASE_KEY_ALIAS="memora"
$env:MEMORA_RELEASE_KEY_PASSWORD="<key-password>"
```

Build:

```powershell
cd "D:\kimi code\AI纪念合照App_MVP计划\android"
.\gradlew.bat :app:assembleRelease
```

If those environment variables are not set, release builds remain unsigned. That is expected for local checks.

## SHA-1 Fingerprints

Current debug SHA-1:

```text
B7:7C:2A:84:CA:4C:FD:98:E2:D7:DE:B4:06:35:65:F4:5A:90:CA:A7
```

Command to verify:

```powershell
keytool -list -v -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore" -storepass android -keypass android
```

Release SHA-1:

```powershell
keytool -list -v -keystore "D:\memora-secrets\memora-release.jks" -alias memora
```

## Google OAuth Clients

Create both OAuth clients in the same Google Cloud project.

### Android OAuth Client

Use:

- Package name: `com.memora.familyphotos`
- SHA-1: debug SHA-1 for local/dev testing
- SHA-1: release SHA-1 for production builds

### Web OAuth Client

Create a Web application OAuth client and copy its Client ID.

Use the Web Client ID in:

- Railway: `GOOGLE_CLIENT_ID=<web-client-id>`
- Android future config: `GOOGLE_WEB_CLIENT_ID=<web-client-id>`

The backend verifies Google ID tokens with `GOOGLE_CLIENT_ID`, so this must be the Web Client ID, not the Android Client ID.

## Railway Variables

Required before real Google Sign-In testing:

```env
USE_MOCK_AUTH=false
GOOGLE_CLIENT_ID=<web-client-id>
JWT_SECRET=<strong-random-secret>
GOOGLE_PLAY_PACKAGE_NAME=com.memora.familyphotos
```

Required before real Google Play Billing verification:

```env
GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_BASE64=<base64-service-account-json>
```

## Google Play Billing

The Play Console app must use package name:

```text
com.memora.familyphotos
```

The backend must verify purchases against:

```env
GOOGLE_PLAY_PACKAGE_NAME=com.memora.familyphotos
```

## Next Implementation Steps

1. Create the release keystore outside the repository.
2. Record debug and release SHA-1 fingerprints.
3. Create Google Cloud Android and Web OAuth clients.
4. Add Railway `GOOGLE_CLIENT_ID`.
5. Implement real Android Google Sign-In with Credential Manager.
6. Implement real Google Play Billing Library integration.
7. Disable mock purchase fallback in production.
