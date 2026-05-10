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

---

## Web MVP (New)

A Next.js web frontend has been added under `web/` for US-market validation. It replicates the core Android flow and connects to the same Express backend.

### Web Tech Stack
- **Framework**: Next.js 16 + React 19 + TypeScript
- **Router**: App Router
- **Styling**: Tailwind CSS v4
- **HTTP Client**: Axios
- **Auth**: Google Identity Services (One Tap)
- **Payments**: Stripe Checkout

### Environment Variables

#### Backend (`backend/.env`)
```bash
# Existing
PORT=3000
NODE_ENV=development
DATABASE_URL=postgresql://...
JWT_SECRET=...
GOOGLE_CLIENT_ID=...         # Web OAuth Client ID (not Android)
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Storage (R2)
STORAGE_ENDPOINT=...
STORAGE_BUCKET=memora-uploads
STORAGE_ACCESS_KEY_ID=...
STORAGE_SECRET_ACCESS_KEY=...
STORAGE_PUBLIC_DOMAIN=https://images.yourdomain.com

# AI Generation
IMAGE_GENERATION_PROVIDER=mock   # or 'openai' for real generation
OPENAI_API_KEY=sk-...

# Stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_PRICE_PREVIEW_PACK=price_...
STRIPE_PRICE_HD_UNLOCK=price_...
STRIPE_PRICE_FULL_PACK=price_...

# Web App URL (for Stripe success/cancel redirects)
WEB_APP_URL=http://localhost:3000
```

#### Web (`web/.env.local`)
```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:3000
NEXT_PUBLIC_GOOGLE_CLIENT_ID=your_google_web_client_id
```

### Local Development

1. **Start Backend**
   ```bash
   cd backend
   cp .env.example .env
   # Edit .env with your credentials
   npm install
   npm run dev        # Runs on http://localhost:3000
   ```

2. **Start Web Frontend**
   ```bash
   cd web
   cp .env.local .env.local
   # Edit .env.local with your credentials
   npm install
   npm run dev        # Runs on http://localhost:3000 (or 3001 if backend uses 3000)
   ```
   Note: If both try to use port 3000, change the web port with `npm run dev -- -p 3001`.

3. **Test Stripe Webhooks Locally**
   ```bash
   # Install Stripe CLI (https://stripe.com/docs/stripe-cli)
   stripe login
   stripe listen --forward-to http://localhost:3000/api/stripe/webhook
   ```
   Copy the `whsec_...` signing secret into `STRIPE_WEBHOOK_SECRET`.

   Then trigger a test event:
   ```bash
   stripe trigger checkout.session.completed
   ```

### Full Local Test Path

1. Open `http://localhost:3000` (web)
2. Sign in with Google (or use `USE_MOCK_AUTH=true` with any long token)
3. Create a project (`/` → "New Project")
4. Upload two photos (`/upload/{projectId}`)
5. Confirm consent (`/consent/{projectId}`)
6. Purchase Preview Pack via Stripe Checkout (use test card `4242 4242 4242 4242`)
7. Return to preview page — webhook grants entitlement
8. Generate preview (`/preview/{projectId}`)
9. Select a candidate
10. Purchase HD Unlock or Full Pack
11. Download result (`/download/{projectId}`)

### Deployment (Railway + Vercel)

#### Backend (Railway)
1. Push repo to GitHub
2. Connect Railway to the `backend/` folder (or root with Dockerfile)
3. Set all environment variables in Railway dashboard
4. Add custom domain: `api.yourdomain.com`
5. Ensure `WEB_APP_URL` points to your production web domain

#### Web Frontend (Vercel)
1. Connect Vercel to the `web/` folder
2. Set environment variables:
   - `NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com`
   - `NEXT_PUBLIC_GOOGLE_CLIENT_ID=...`
3. Add custom domain: `yourdomain.com`
4. Update backend `ALLOWED_ORIGINS` to include the production web domain

#### Stripe Production
1. Switch to Stripe Live mode
2. Create live prices for preview_pack, hd_unlock, full_pack
3. Update `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, and price IDs in Railway
4. Register webhook endpoint: `https://api.yourdomain.com/api/stripe/webhook`
5. Select event: `checkout.session.completed`

### Security Notes
- Never commit real API keys. Use `.env` files and Railway/Vercel secrets.
- Stripe webhook verifies `Stripe-Signature` with raw body.
- Webhook fulfillment is idempotent (duplicate events do not re-grant entitlements).
- All project/payment endpoints require JWT auth and project ownership checks.
- OpenAI API key is server-side only; frontend never sees it.
- `IMAGE_GENERATION_PROVIDER=mock` is safe for testing. In production with `openai`, a missing `OPENAI_API_KEY` will throw an error instead of silently falling back to mock.
