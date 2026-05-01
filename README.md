# AI Memorial Photo App

An Android-first app that helps users create AI-composed memorial photos with deceased loved ones. Optional gentle animated memorial video.

## Project Structure

```
memorial-app/
  android/          # Native Android App (Kotlin + Jetpack Compose)
  backend/          # Node.js/TypeScript API & Worker
  video-renderer/   # Remotion + FFmpeg video rendering
  docs/             # Product spec, API docs, task tracking
```

## Prerequisites

- **Android**: Android Studio (latest stable), JDK 17+
- **Backend**: Node.js 20+, pnpm or npm
- **Video**: Node.js 20+, FFmpeg installed and on PATH

## Quick Start

### 1. Backend

```bash
cd backend
cp .env.example .env
# Edit .env with your values
pnpm install
pnpm dev
```

Server runs at `http://localhost:3000`.

### 2. Android

Open `android/` in Android Studio and run on emulator or device.

### 3. Video Renderer (optional)

```bash
cd video-renderer
pnpm install
# Render with sample image
pnpm render --input sample.png --output out.mp4
```

## Environment Variables

See `.env.example` files in each module:
- `backend/.env.example`
- `video-renderer/.env.example`

**Never commit real credentials.**

## Development Workflow

We work in milestones. Check `docs/tasks.md` for current progress.

Each milestone targets a single verifiable goal. After each milestone:
1. List modified files
2. Describe implemented features
3. Run tests/builds
4. Note risks or incomplete items

## Key Decisions

- **Android**: Native Kotlin (not React Native/Flutter) for best Google Play Billing and Photo Picker support.
- **Backend**: Express + TypeScript for rapid development.
- **Image Gen**: OpenAI `gpt-image-2` via server-side only.
- **Video**: Remotion + FFmpeg server-side; no on-device rendering.
- **Payment**: Google Play Billing only; no Stripe or external checkout.

## License & Ethics

This is a private memorial tool. All outputs are labeled AI-generated. No talking, voice cloning, or public sharing features.
