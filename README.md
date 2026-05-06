# Memora - AI Family Memory Photo App

Memora is an Android-first AI Family Memory Photo App for international users. It helps families complete meaningful group photos when relatives or close friends could not be present at a trip, party, holiday, wedding, graduation, birthday, reunion, or other milestone event.

Users upload an event/base photo and a person reference photo. The app generates natural AI-assisted family memory photos, supports basic organization by time, activity, and person type, and can later package selected projects into a photo memory album.

## Product Direction

- Private family memory use, not a public gallery.
- Natural group-photo completion, not entertainment deepfake software.
- Clear AI-generated disclosure.
- User-controlled upload, preview, purchase, download, and deletion flow.
- Manual organization in the MVP; AI-assisted tags can be added later through `detectedTags`.

## Project Structure

```text
memora/
  android/          Native Android app (Kotlin + Jetpack Compose)
  backend/          Node.js / TypeScript API and generation worker
  video-renderer/   Remotion + FFmpeg rendering prototype
  docs/             Product spec, API docs, deployment notes, task tracking
```

## Current MVP Status

Implemented:

- Android app shell with Jetpack Compose navigation.
- Android Photo Picker integration for event/base and person reference photos.
- Backend API deployed on Railway.
- Postgres project/user/purchase schema.
- Cloudflare R2 private bucket storage with signed URLs.
- Multipart photo upload to R2.
- Mock AI candidate generation.
- Mock purchase verification with Google Play fallback logic.
- Generation status polling and candidate selection.
- Prompt optimizer endpoint.
- Contact/feedback endpoint.
- Album API foundation with mock render flow.
- Project metadata foundation: `eventDate`, `activityType`, `personTypes`, `detectedTags`, and `albumId`.
- Legacy upload compatibility: old `deceased` maps to new `person`, old `living` maps to new `base`.

Not yet complete:

- Real image-generation provider integration for production outputs.
- Android UI for full year/month, activity, and person filters.
- Android UI for album creation and album status.
- Real Google Play Billing integration.
- Production PDF album rendering.
- Production MP4 page-turn rendering.
- Watermark and AI-generated labels on all outputs.
- Complete privacy policy and account/data deletion closure.
- Real HD photo upscaling or final-generation pipeline.

## Core User Flow

1. Sign in with Google.
2. Create a family memory project.
3. Add event date, activity type, and person type.
4. Upload an event/base photo.
5. Upload a person reference photo.
6. Select a style, such as Natural Family, Travel Memory, Party Gathering, Holiday Celebration, or Milestone Event.
7. Accept consent for photo rights, private family use, and AI-generated output.
8. Generate 2-4 low-resolution watermarked previews.
9. Unlock HD photo or HD photo + album/video bundle through Google Play Billing.
10. Download, save, or share the result.

## Photo Organization and Albums

The MVP direction includes project management by:

- Year and month.
- Activity type.
- Person type.

Albums can be created from selected projects or from the current filtered project set. The first album version should focus on a practical PDF photo book and optional page-turn MP4 package.

## Visual Direction

Use the Natural Photo direction:

- Warm off-white background.
- Film green primary color.
- Natural photo cards.
- Compact filters and practical project controls.
- Calm family-album mood.
- No heavy purple or somber memorial styling.

## Prerequisites

- Android: Android Studio latest stable and JDK 17+.
- Backend: Node.js 20+ and pnpm or npm.
- Video renderer: Node.js 20+ and FFmpeg on PATH.

## Quick Start

### Backend

```bash
cd backend
cp .env.example .env
# Edit .env with local or staging values.
pnpm install
pnpm dev
```

The local server runs at:

```text
http://localhost:3000
```

### Android

Open `android/` in Android Studio and run the app on an emulator or physical device.

### Video Renderer

```bash
cd video-renderer
pnpm install
pnpm render --input sample.png --output out.mp4
```

## Environment Variables

See the module-level examples:

- `backend/.env.example`
- `video-renderer/.env.example`

Never commit real credentials.

## Useful Checks

Backend:

```bash
cd backend
npm run typecheck
```

Android:

```powershell
cd android
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
```

## Key Decisions

- Android first, not web first.
- Native Kotlin, not React Native or Flutter, for the MVP.
- Backend required; the Android app must not call image-generation APIs directly.
- Server-side rendering for PDF albums and MP4 outputs.
- Google Play Billing for in-app digital purchases.
- Free previews are low-resolution and watermarked.
- Paid products unlock HD photos and optional album/video packages.
- Product positioning is private family memory completion with practical organization by time, activity, and person type.

## Trust and Safety

Memora is a private family memory tool. Outputs should be labeled as AI-generated. The product must not support public-figure impersonation, deceptive historical claims, unauthorized real-person use, public galleries, voice cloning, chatbots, talking portraits, or full-body reenactment.
