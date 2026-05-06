# Memora - Family Memory Photo App - Product Spec

## Overview

Memora is an Android-first app for users who want to complete family event photos when relatives or close friends could not be present. Users upload a base scene photo and a person reference photo, and the app generates an AI-composed family memory photo. Optional: a gentle animated memory album video.

## Positioning

- **Platform**: Android native (Google Play)
- **Tech**: Kotlin + Jetpack Compose, Node.js/TypeScript backend
- **Monetization**: Free low-res watermarked preview; paid unlock for HD photo or HD photo + video
- **Ethics**: Private family memory only. No posing as real historical photos, no public gallery, no celebrity impersonation.

## User Flow (MVP)

1. Open app -> Google Sign-In.
2. Create a memory project.
3. Select activity date, activity type, and person types.
4. Pick photos via Android Photo Picker:
   - Base scene photo (event/group photo).
   - Person reference photo.
5. Choose style: `NATURAL_FAMILY`, `TRAVEL_MEMORY`, `PARTY_GATHERING`, `HOLIDAY_CELEBRATION`, or `MILESTONE_EVENT`.
6. Filter projects by year/month, activity type, or person type.
7. Accept consent checkboxes for rights, private use, and AI-generated understanding.
8. Cloud generates 2-4 low-res watermarked previews.
9. User selects one preview.
10. Purchase via Google Play Billing:
    - HD photo.
    - HD photo + memory album video.
11. Cloud generates HD assets.
12. User downloads, saves, or shares.

## Free vs Paid

| Feature | Free | Paid |
|---------|------|------|
| Upload photos | Yes | Yes |
| Select style | Yes | Yes |
| Low-res watermarked preview | Yes | Yes |
| Delete project and data | Yes | Yes |
| Download HD photo | No | Yes |
| Download memory album video | No | Yes |
| Revisit purchased results | No | Yes |

## Technical Stack

- **Android**: Kotlin, Jetpack Compose, Navigation Compose, MVVM, Coroutines/Flow
- **Backend**: Node.js + TypeScript, Express
- **Storage**: Supabase Storage or Cloudflare R2
- **Image Gen**: server-side image-generation provider abstraction
- **Video**: Remotion + FFmpeg (server-side only)
- **Auth**: Google Sign-In + app session
- **Billing**: Google Play Billing

## Classification Management

### Implemented

- **Manual tagging** - Activity type (single-select), person types (multi-select), and event date can be specified when creating a project.
- **Project filtering by tags** - Home screen supports filtering projects by year/month, activity type, or person type.
- **Project card labels** - Event date, activity type tag, and person type tags are displayed on project cards.

### Reserved for Future

- **AI auto-recognition** (`detectedTags`) - Reserved field for future AI-powered automatic classification of project content.

## Album Route

- Album data model and API exist as a foundation.
- Real PDF/MP4 rendering remains future work.
- Album completion notifications remain future work.

## Content Boundaries

- No celebrities or public figures.
- No posing as real historical photos.
- No NSFW, hate, or harassment.
- No commercial unauthorized use.
- All outputs labeled "AI-generated".

## Data Retention

- Uploaded originals: 30 days.
- Generated results: 30 days, extendable for paid assets.
- Full data deletion on request.
