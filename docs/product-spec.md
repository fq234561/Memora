# AI Memorial Photo App - Product Spec

## Overview
An Android-first app for adult users who have lost a loved one. Users upload photos of themselves and the deceased, and the app generates an AI-composed memorial photo they never had the chance to take. Optional: a gentle animated memorial video.

## Positioning
- **Platform**: Android native (Google Play)
- **Tech**: Kotlin + Jetpack Compose, Node.js/TypeScript backend
- **Monetization**: Free low-res watermarked preview; paid unlock for HD photo or HD photo + video
- **Ethics**: Private memorial only. No talking, no voice clone, no chatbot, no full-body resurrection, no public gallery.

## User Flow (MVP)
1. Open app → Google Sign-In
2. Create a memorial project
3. Pick photos via Android Photo Picker:
   - Deceased reference photo
   - User/family reference photo
4. Choose style (natural family / vintage restore / birthday / graduation-wedding-holiday)
5. Consent checkboxes (rights, private use, AI-generated understanding)
6. Cloud generates 2-4 low-res watermarked previews
7. User selects one
8. Purchase via Google Play Billing:
   - HD photo
   - HD photo + memorial video
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
| Download memorial video | ❌ | ✅ |
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
- No children's market targeting
- All outputs labeled "AI-generated"

## Data Retention
- Uploaded originals: 30 days
- Generated results: 30 days (extendable for paid assets)
- Full deletion on user request
