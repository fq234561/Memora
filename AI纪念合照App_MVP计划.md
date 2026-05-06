# AI Family Memory Photo App - Android MVP Plan

## 1. Product Overview

This product is an Android-first AI Family Memory Photo App for international users. It helps families complete meaningful group photos when relatives or close friends could not be present at a trip, party, holiday, wedding, graduation, birthday, reunion, or other milestone event.

Users upload an event/base photo and one or more person reference photos. The app generates natural AI-assisted family memory photos, then can optionally generate a family album package such as a PDF photo book or a short page-turn MP4.

The product is not positioned as entertainment deepfake software. It is a private, consent-based, family-memory tool with clear AI-generated disclosure and data deletion controls.

### Core Positioning

- Product category: AI Family Memory Photo App.
- First platform: Android native app for Google Play.
- Target users: adults who want to complete private family, travel, party, holiday, reunion, graduation, wedding, birthday, or milestone photos.
- Core capability: AI group-photo completion, organized memory management, and optional album generation.
- Monetization: free low-resolution watermarked previews, paid HD photo unlock, and optional HD photo + album/video bundle.
- Tech direction: Kotlin + Jetpack Compose Android app, TypeScript backend, cloud storage, image generation service, and server-side album/video rendering.
- Future expansion: iOS SwiftUI app reusing the same backend API.

### Non-Goals

- No voice cloning.
- No chatbot.
- No talking portrait.
- No full-body motion reenactment.
- No celebrity or public-figure use cases.
- No public gallery.
- No claim that AI output is a real historical photo.
- No direct client-side access to image-generation API keys.

---

## 2. MVP Feature Scope

### Main User Flow

1. User opens the app and signs in with Google.
2. User creates a family memory project.
3. User adds basic organization metadata:
   - Event date.
   - Activity type.
   - Person type.
4. User selects photos with Android Photo Picker:
   - Event/base photo, such as a trip, party, family table, ceremony, or group scene.
   - Person reference photo for the person to be added.
5. User chooses a photo style:
   - Natural Family.
   - Travel Memory.
   - Party Gathering.
   - Holiday Celebration.
   - Milestone Event.
6. User accepts lightweight consent:
   - I have the right to use these photos.
   - This is for private family memory use.
   - I understand the result is AI-generated.
7. Backend generates 2-4 low-resolution watermarked previews.
8. User selects a preferred preview.
9. User unlocks one of the paid products through Google Play Billing:
   - HD photo.
   - HD photo + album/video bundle.
10. Backend generates final HD assets and optional album/video output.
11. User downloads, saves, or shares the generated result.

### Photo Organization

The MVP should include basic photo organization because the product is centered on family memory management, not only one-off generation.

- Projects can be managed by year and month.
- Projects can be managed by activity type.
- Projects can be managed by person type.
- The first version uses manual user selection.
- `detectedTags` is reserved for later AI-assisted suggestions and should not block manual editing.
- Home should expose compact filters for year/month, activity type, and person type.
- Albums can be created from selected projects or from the current filtered set.

### Free and Paid Boundaries

Free users can:

- Create projects.
- Upload selected photos.
- Choose style and organization metadata.
- Generate low-resolution watermarked previews.
- Delete projects and uploaded data.

Paid users can:

- Download HD photos.
- Download optional album/video package.
- Revisit purchased outputs from project history while retained.

---

## 3. Visual Design Direction

The MVP should use Kimi Option B: Natural Photo.

### Design Mood

- Calm family-album feeling.
- Warm, natural, and international.
- Avoid a heavy purple or somber visual tone.
- Prioritize real photo surfaces, clean filters, and practical project management.

### Suggested Palette

- Background: warm off-white.
- Surface: white.
- Primary: film green.
- Primary soft: light sage.
- Accent: soft warm amber.
- Text primary: charcoal.
- Text secondary: muted gray-green.
- Divider: warm neutral.

### UI Principles

- Use compact filters and clear cards instead of decorative landing-page sections.
- Use natural photo thumbnails as the main visual signal.
- Use low-radius cards and restrained shadows.
- Keep controls practical: chips for activity/person type, segmented controls for filters, icon buttons for actions, and clear CTAs for generation and downloads.
- Do not use in-app text that explains the visual style or design concept.

### Core Screens

- Home: project list, latest projects, year/month filters, activity filters, person filters, and album entry.
- Create Project: title, event date, activity type, person type, and style selection.
- Upload Photos: event/base photo and person reference photo.
- Consent: rights, private family use, and AI-generated disclosure.
- Preview: 2-4 watermarked preview choices.
- Download: HD photo, optional album/video status, save, and share.
- Albums: create album from selected projects or current filters, render status, PDF/MP4 download when ready.

---

## 4. Recommended Technical Architecture

### Android Client

Recommended stack:

- Kotlin.
- Jetpack Compose.
- Navigation Compose.
- MVVM.
- Kotlin Coroutines and Flow.
- Google Sign-In.
- Google Play Billing.
- Android Photo Picker.

Why native Android first:

- Best Google Play Billing integration.
- Best Photo Picker and storage permission behavior.
- Mature background upload, download, and sharing capabilities.
- Easier Google Play compliance.
- Future iOS app can reuse backend APIs while using native SwiftUI.

### Backend

Recommended stack:

- TypeScript backend service.
- Postgres for relational data.
- Cloud storage for uploaded photos and generated assets.
- Queue or job system for generation tasks.
- Image-generation provider abstraction.
- Remotion + FFmpeg or equivalent server-side renderer for album/video output.

Backend responsibilities:

- Authentication and session validation.
- Signed upload URLs.
- Project and asset management.
- Image-generation orchestration.
- Album and video rendering.
- Google Play purchase-token verification.
- Download entitlement checks.
- Data deletion.
- Job retry and failure tracking.

---

## 5. Android App Structure

### Main Screens

- Sign In.
- Home / Project List.
- Create Project.
- Upload Photos.
- Style Selection.
- Consent.
- Generation Progress.
- Preview Selection.
- Purchase.
- Download Result.
- Project History.
- Albums.
- Settings.
- Data Deletion.
- Feedback / Report.

### Key Screen Notes

#### Sign In

- Use Google Sign-In.
- Backend validates Google token.
- Backend returns app session.
- Later API calls include the app session token.

#### Create Project

- User enters a project title.
- User selects event date.
- User selects activity type.
- User selects one or more person types.
- User selects photo style.

#### Upload Photos

- Use Android Photo Picker.
- Do not request full gallery permission.
- Compress and validate selected images before upload when needed.
- Use these labels:
  - Event Photo.
  - Person Reference Photo.

#### Consent

User must accept:

- I have the right to use these photos.
- This is for private family memory use.
- I understand the result is AI-generated.

Backend records:

- Consent text version.
- User ID.
- Project ID.
- Timestamp.
- IP address when available.
- Device information when available.

#### Preview

- Show 2-4 low-resolution watermarked previews.
- User selects one version for unlock.
- Unpaid users cannot access HD files.

#### Download

- Show final HD photo.
- Show album/video generation status if purchased.
- Support saving to local device.
- Support system share.
- Show AI-generated labeling in a clear but not intrusive way.

---

## 6. API Design

All app APIs should be exposed under the `/api` prefix.

### Authentication

```http
POST /api/auth/google
```

Purpose:

- Receive Android Google sign-in token.
- Verify token.
- Create or find user.
- Return app session.

### List Projects

```http
GET /api/projects?year=2026&month=5&activityType=TRAVEL_MEMORY&personType=FAMILY_MEMBER
```

Purpose:

- List the signed-in user's projects.
- Support optional filters by year, month, activity type, and person type.
- Return projects sorted by newest first.

### Create Project

```http
POST /api/projects
```

Request:

```json
{
  "title": "Family Trip Memory",
  "style": "TRAVEL_MEMORY",
  "locale": "en-US",
  "eventDate": "2026-05-01",
  "activityType": "TRAVEL",
  "personTypes": ["FAMILY_MEMBER"]
}
```

Response:

- Project object with generated ID and initial status.

### Upload Photos

```http
POST /api/projects/{projectId}/upload
```

Request:

```json
{
  "type": "base",
  "fileName": "family-trip.jpg",
  "mimeType": "image/jpeg",
  "sizeBytes": 2450000
}
```

Supported upload types:

- `base`: event/base photo.
- `person`: person reference photo.

Legacy compatibility:

- Old `living` upload type maps to new `base`.
- Old `deceased` upload type maps to new `person`.

### Create Preview Job

```http
POST /api/projects/{projectId}/preview
```

Purpose:

- Create low-resolution watermarked preview generation job.
- Use project photos, selected style, activity type, and person types.
- Output 2-4 preview images.

### Get Project

```http
GET /api/projects/{projectId}
```

Returns:

- Project metadata.
- Uploaded assets.
- Preview assets.
- Order status.
- Final HD assets.
- Album/video assets.
- Generation job status.

### Check Project Status

```http
GET /api/projects/{projectId}/status
```

Purpose:

- Poll project generation state.
- Return preview/final job progress and asset readiness.

### Verify Google Play Purchase

```http
POST /api/billing/google/verify
```

Request:

```json
{
  "projectId": "project_123",
  "productId": "family_memory_album_bundle",
  "purchaseToken": "google_purchase_token"
}
```

Purpose:

- Verify purchase token with Google Play Developer API.
- Bind entitlement to the project.
- Return entitlement state.

### Generate Final Result

```http
POST /api/projects/{projectId}/finalize
```

Purpose:

- Paid users only.
- Generate HD photo.
- Generate album/video package if the purchased entitlement includes it.

### Albums

```http
POST /api/albums
GET /api/albums
GET /api/albums/{albumId}
POST /api/albums/{albumId}/render
GET /api/albums/{albumId}/status
DELETE /api/albums/{albumId}
```

Purpose:

- Create a memory album from selected project IDs or the current filtered set.
- Render PDF album and optional page-turn MP4.
- Track render status.
- Delete album and unlink related projects.

### Delete Project

```http
DELETE /api/projects/{projectId}
```

Purpose:

- Delete project record.
- Delete original uploads.
- Delete preview images.
- Delete final images.
- Delete video and album references where applicable.

### Feedback / Report

```http
POST /api/reports
```

Purpose:

- Allow users to report inappropriate, incorrect, unauthorized, or abusive content.
- Support Google Play requirements for AI-generated content feedback/reporting.

---

## 7. Data Model

### users

Stores basic user information.

Suggested fields:

- `id`.
- `google_user_id`.
- `email`.
- `display_name`.
- `country`.
- `created_at`.
- `last_login_at`.

### projects

Stores family memory projects.

Suggested fields:

- `id`.
- `user_id`.
- `title`.
- `style`.
- `status`.
- `locale`.
- `event_date`.
- `activity_type`.
- `person_types`.
- `detected_tags`.
- `album_id`.
- `retention_until`.
- `created_at`.
- `updated_at`.

Project statuses:

- `draft`.
- `uploaded`.
- `preview_queued`.
- `preview_generating`.
- `preview_ready`.
- `payment_pending`.
- `paid`.
- `final_generating`.
- `final_ready`.
- `failed`.
- `deleted`.

### assets

Stores uploaded photos, preview images, final images, videos, and album outputs.

Suggested fields:

- `id`.
- `project_id`.
- `user_id`.
- `type`.
- `storage_path`.
- `width`.
- `height`.
- `mime_type`.
- `size_bytes`.
- `is_paid_asset`.
- `created_at`.

Asset types:

- `base_reference`: event/base photo.
- `person_reference`: person reference photo.
- `preview_image`.
- `final_image`.
- `final_video`.
- `album_pdf`.
- `album_video`.
- `watermark_preview`.

Legacy field compatibility:

- Old database field `living_photo_key` is semantically the new base photo.
- Old database field `deceased_photo_key` is semantically the new person reference photo.
- New UI and API copy should not expose old names.

### albums

Stores generated memory albums.

Suggested fields:

- `id`.
- `user_id`.
- `title`.
- `project_ids`.
- `status`.
- `pdf_key`.
- `mp4_key`.
- `created_at`.
- `updated_at`.

Album statuses:

- `draft`.
- `render_queued`.
- `rendering`.
- `ready`.
- `failed`.
- `deleted`.

### generation_jobs

Tracks generation tasks.

Suggested fields:

- `id`.
- `project_id`.
- `job_type`.
- `model`.
- `prompt`.
- `status`.
- `error_message`.
- `retry_count`.
- `estimated_cost`.
- `started_at`.
- `finished_at`.

Job types:

- `preview_image`.
- `final_image`.
- `album_pdf`.
- `album_video`.

### orders

Stores Google Play purchase records.

Suggested fields:

- `id`.
- `user_id`.
- `project_id`.
- `product_id`.
- `purchase_token_hash`.
- `google_order_id`.
- `status`.
- `entitlement`.
- `purchased_at`.
- `verified_at`.

Order statuses:

- `pending`.
- `verified`.
- `acknowledged`.
- `refunded`.
- `revoked`.
- `failed`.

### consent_records

Stores accepted consent.

Suggested fields:

- `id`.
- `user_id`.
- `project_id`.
- `consent_version`.
- `consent_text`.
- `ip_address`.
- `device_info`.
- `accepted_at`.

### reports

Stores user feedback and reports.

Suggested fields:

- `id`.
- `user_id`.
- `project_id`.
- `reason`.
- `message`.
- `status`.
- `created_at`.
- `resolved_at`.

---

## 8. Image Generation Strategy

### Model and Provider

Image generation should run only on the backend. The backend can use an image-generation provider abstraction so the concrete provider can be changed by configuration.

Primary use:

- Generate natural family group-photo completions from uploaded references.
- Match the user's selected activity and style.
- Preserve identity cues from references as much as the provider allows.
- Output preview and HD variants.

### Preview Generation

Goals:

- Low cost.
- Fast feedback.
- Prevent bypassing payment by screenshotting full-quality assets.

Strategy:

- Generate 2-4 low-resolution preview versions.
- Add visible watermark.
- Reduce resolution.
- Do not expose original final assets before purchase.

### HD Generation

Goals:

- Provide high-quality paid result.
- Keep identity and composition stable.
- Clearly label output as AI-generated.

Strategy:

- Use higher-quality settings where available.
- Output high-resolution image.
- Add small AI-generated marker or metadata where appropriate.
- Preserve paid asset download access according to retention policy.

### Prompt Direction

Prompts should emphasize:

- Private family memory photo.
- Natural group photo composition.
- Warm and realistic family-album style.
- Preserve facial identity from references.
- Avoid uncanny expressions.
- Avoid text artifacts.
- Do not imply that the output is documentary evidence.
- Respect the selected activity type and person type.

---

## 9. Album and Video Strategy

### Recommended Approach

Use server-side rendering for album and video output. Android should not render final PDF/MP4 packages locally.

Android app responsibilities:

- Show render status.
- Preview generated output when available.
- Download PDF/MP4.
- Share output through the system share sheet.

Backend responsibilities:

- Collect selected project outputs.
- Render PDF album layout.
- Render optional page-turn MP4.
- Upload generated assets to storage.
- Return signed download URLs.

### Why Not Generate Album/Video Locally On Android

- Device performance varies widely.
- Battery and heat are hard to control.
- App backgrounding can interrupt rendering.
- Failures are harder to retry.
- iOS cannot reuse Android-local rendering logic.
- Backend rendering provides consistent watermarking and quality.

### First Album Format

Suggested PDF:

- Cover page.
- Project title and date.
- Photo pages grouped by month or activity.
- AI-generated disclosure.

Suggested MP4:

- H.264 MP4.
- 1080x1920 vertical by default.
- Optional 1920x1080 landscape.
- 30 fps.
- 8-12 seconds for MVP.
- No audio by default.
- Page-turn or gentle camera animation.

### Future Enhancements

V1.5 can add:

- Better layout templates.
- Auto-generated captions.
- 2.5D parallax from still images.
- Region-aware album themes.
- AI-assisted tag suggestions.

These are not required for the MVP.

---

## 10. Google Play Billing Design

### Suggested Products

#### family_memory_hd_unlock

Unlocks HD photo for one project.

Suggested price:

- USD 9.99.

#### family_memory_album_bundle

Unlocks HD photo plus PDF album and optional page-turn MP4 for one project or album package.

Suggested price:

- USD 14.99.

Actual prices should be localized in Play Console.

### Purchase Flow

1. User selects a preview or album package.
2. App starts Google Play Billing.
3. User completes purchase.
4. App sends purchase token to backend.
5. Backend verifies purchase token with Google Play Developer API.
6. Backend creates entitlement.
7. App acknowledges purchase.
8. Backend starts final generation or album render job.
9. App polls project or album status until output is ready.

### Billing Notes

- In-app digital content should use Google Play Billing.
- Do not direct users to Stripe or external payment pages inside the app.
- Backend must verify purchase tokens.
- Order handling must be idempotent.
- Refunded or revoked entitlements must be handled.

---

## 11. Compliance and Trust Boundaries

### Minimum Rules

The first version uses lightweight but explicit rules:

- User declares they have rights to use uploaded photos.
- User confirms private family memory use.
- User understands output is AI-generated.
- Output is labeled AI-generated.
- Project deletion is available.
- Feedback/reporting is available.

### Content Boundaries

Prohibited or restricted:

- Celebrity or public-figure generation.
- Impersonation of real historical documentation.
- Adult or sexual content.
- Hate, harassment, or deceptive content.
- Unauthorized use of real people.
- Child-targeted market positioning.
- Public gallery or public discovery feed.

### Data Retention

Suggested defaults:

- Original uploads retained for 30 days.
- Generated results retained for 30 days.
- Paid assets may remain accessible through project history while retained.
- Users can delete projects.
- Deleting a project should delete related database records and stored assets where applicable.

---

## 12. Test Plan

### Android Tests

- Google Sign-In success and failure.
- Session expiry and refresh behavior.
- Photo Picker with JPG, PNG, and WebP.
- Reject corrupt, too-small, or too-large images.
- Create project with event date, activity type, and person types.
- Filter project list by year/month, activity type, and person type.
- Upload event/base photo and person reference photo.
- Weak network upload retry.
- App background/resume during generation polling.
- Download HD image and album/video output.
- System share.
- Delete project.

### Backend Tests

- Signed upload URL expiry.
- User cannot access another user's project.
- `POST /api/projects` persists organization metadata.
- `GET /api/projects` filters by year/month, activity type, and person type.
- Legacy upload mapping:
  - `deceased` maps to `person`.
  - `living` maps to `base`.
- Generation job failure retry.
- Idempotent purchase verification.
- Project deletion removes or invalidates assets.
- Album deletion clears related project `albumId`.

### Billing Tests

- Google Play test product purchase.
- Purchase cancellation.
- Purchase failure.
- Duplicate purchase token.
- Verification retry.
- Refund entitlement revocation.
- Unpaid user cannot access HD assets.

### Album Tests

- Create album from selected projects.
- Create album from current filters.
- Render PDF album.
- Render MP4 page-turn video.
- Poll album status.
- Delete album and confirm projects are unlinked.

### Review / Store Tests

- No external payment guidance inside app.
- AI-generated outputs are labeled.
- Privacy policy entry exists.
- Data deletion entry exists.
- Feedback/reporting entry exists.
- Play Console test account and test purchase notes are prepared.

---

## 13. Development Milestones

### Milestone 1: Product Skeleton

- Android native project setup.
- Google Sign-In.
- Project creation.
- Organization metadata: event date, activity type, person type.
- Photo Picker upload.
- Backend project and asset tables.

### Milestone 2: Project Management and Filters

- Home project list.
- Year/month filter.
- Activity type filter.
- Person type filter.
- Project detail page.
- Manual metadata editing where needed.

### Milestone 3: Preview Generation

- Upload photos to cloud storage.
- Backend image-generation provider integration.
- Low-resolution watermarked previews.
- App preview selection.

### Milestone 4: In-App Purchase Unlock

- Google Play Billing integration.
- Backend purchase-token verification.
- Entitlement creation.
- Paid HD image generation.

### Milestone 5: Albums

- Create album from selected projects or filters.
- Render PDF album.
- Render optional MP4 page-turn video.
- Download and share album outputs.

### Milestone 6: Compliance and Launch Prep

- Privacy policy.
- Data deletion.
- Feedback/reporting.
- AI-generated disclosure.
- Play Console configuration.
- Internal testing release.

---

## 14. Key Decisions

- Build Android first, not web first.
- Use native Kotlin, not React Native or Flutter, for the MVP.
- Backend is required; the Android app must not call image-generation APIs directly.
- Use an image-generation provider abstraction on the backend.
- Use server-side rendering for PDF albums and MP4 outputs.
- Use Google Play Billing for in-app digital purchases.
- Free previews are low-resolution and watermarked.
- Paid products unlock HD photos and optional album/video packages.
- Product positioning is private family memory completion, with practical organization by time, activity, and person type.
