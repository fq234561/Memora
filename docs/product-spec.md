# Memora - Family Memory Photo App - Product Spec

## Overview
An Android-first app for users who want to naturally include absent loved ones in family event photos. Users upload a base scene photo and a person reference photo, and the app generates an AI-composed family memory photo. Optional: a gentle animated memory album video.

## Positioning
- **Platform**: Android native (Google Play)
- **Tech**: Kotlin + Jetpack Compose, Node.js/TypeScript backend
- **Monetization**: Free low-res watermarked preview; paid unlock for HD photo or HD photo + video
- **Ethics**: Private family memory only. No posing as real historical photos, no public gallery, no celebrity impersonation.

## User Flow (MVP)
1. Open app → Google Sign-In
2. Create a memory project
3. Pick photos via Android Photo Picker:
   - Base scene photo (event/group photo)
   - Person reference photo (absent loved one)
4. Choose style (NATURAL_FAMILY / TRAVEL_MEMORY / PARTY_GATHERING / HOLIDAY_CELEBRATION / MILESTONE_EVENT)
5. Consent checkboxes (rights, private use, AI-generated understanding)
6. Cloud generates 2-4 low-res watermarked previews
7. User selects one
8. Purchase via Google Play Billing:
   - HD photo
   - HD photo + memory album video
9. Cloud generates HD assets
10. User downloads, saves, or shares

## Free vs Paid
| Feature | Free | Paid |
|---------|------|------|
| Upload photos | ✅ | ✅ |
| Select style | ✅ | ✅ |
| Low-res watermarked preview | ✅ | ✅ |
| Delete project & data | ✅ | ✅ |
| Download HD photo | ❌ | ✅ |
| Download memory album video | ❌ | ✅ |
| Revisit purchased results | ❌ | ✅ |

## Technical Stack
- **Android**: Kotlin, Jetpack Compose, Navigation Compose, MVVM, Coroutines/Flow
- **Backend**: Node.js + TypeScript, Express
- **Storage**: Supabase Storage or Cloudflare R2
- **Image Gen**: OpenAI `gpt-image-2` (server-side only)
- **Video**: Remotion + FFmpeg (server-side only)
- **Auth**: Google Sign-In + App session
- **Billing**: Google Play Billing

## Content Boundaries
- No celebrities / public figures
- No posing as real historical photos
- No NSFW, hate, or harassment
- No commercial unauthorized use
- All outputs labeled "AI-generated"

## Data Retention
- Uploaded originals: 30 days
- Generated results: 30 days (extendable for paid assets)
- Full d