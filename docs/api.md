# API Contract

## Base URL

| Environment | URL |
|------------|-----|
| Development | `http://localhost:3000` |
| Production | `https://memora-production-8d49.up.railway.app/` |

All endpoints below are prefixed with `/api`.

## Authentication

All endpoints except `/api/auth/google` and `/api/health` require header:

```
Authorization: Bearer <accessToken>
```

The `accessToken` is a JWT returned by `POST /api/auth/google`.

---

### POST /api/auth/google

Authenticate with Google ID token (or a mock identifier when `USE_MOCK_AUTH=true`) and receive a JWT access token.

**Request:**
```json
{
  "idToken": "<google_id_token_or_mock_identifier>"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "<jwt_access_token>",
    "user": {
      "id": "user_123",
      "email": "user@example.com",
      "name": "User Name",
      "avatarUrl": null,
      "createdAt": "2026-04-30T12:00:00Z"
    }
  }
}
```

---

## Projects

All project endpoints require authentication.

### GET /api/projects

List all projects for the authenticated user.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "project_123",
      "userId": "user_123",
      "title": "For Mom",
      "style": "NATURAL_FAMILY",
      "deceasedPhotoUrl": "https://...",  // semantic: personPhotoUrl (legacy field,兼容)
      "livingPhotoUrl": "https://...",    // semantic: basePhotoUrl (legacy field,兼容)
      "generatedPhotoUrl": null,
      "hdPhotoUrl": null,
      "status": "DRAFT",
      "consentGiven": false,
      "regenerationCount": 0,
      "regenerationLimit": 0,
      "candidateUrls": null,
      "selectedCandidateIndex": null,
      "purchasedProductId": null,
      "generationHistory": [],
      "createdAt": "2026-04-30T12:00:00Z",
      "updatedAt": "2026-04-30T12:00:00Z"
    }
  ]
}
```

### POST /api/projects

Create a new memory project.

**Request:**
```json
{
  "title": "For Mom",
  "style": "NATURAL_FAMILY"
}
```

**`style` enum:** `NATURAL_FAMILY`, `TRAVEL_MEMORY`, `PARTY_GATHERING`, `HOLIDAY_CELEBRATION`, `MILESTONE_EVENT`

**Response:** `201 Created` — same shape as a single project object in `GET /api/projects`.

### GET /api/projects/:id

Get project details. Photo URLs are signed URLs (15-minute TTL) when stored in R2.

**Response:** same shape as a single project object.

### POST /api/projects/:id/upload

Upload a photo file directly via multipart/form-data to R2 storage.

**Request:** `multipart/form-data`
- `photo` — image file (JPG, PNG, or WebP, max 20MB)
- `type` — `"base"` or `"person"` (legacy values `"deceased"` and `"living"` are accepted for backward compatibility)

**Response:**
```json
{
  "success": true,
  "data": {
    "project": {
      "id": "project_123",
      "userId": "user_123",
      "title": "For Mom",
      "style": "NATURAL_FAMILY",
      "deceasedPhotoUrl": "https://...",  // semantic: personPhotoUrl (legacy field,兼容)
      "livingPhotoUrl": "https://...",    // semantic: basePhotoUrl (legacy field,兼容)
      "generatedPhotoUrl": null,
      "hdPhotoUrl": null,
      "status": "UPLOADED",
      "consentGiven": false,
      "regenerationCount": 0,
      "regenerationLimit": 0,
      "candidateUrls": null,
      "selectedCandidateIndex": null,
      "purchasedProductId": null,
      "createdAt": "2026-04-30T12:00:00Z",
      "updatedAt": "2026-04-30T12:05:00Z"
    }
  }
}
```

When both `base` and `person` photos are uploaded, `status` automatically changes to `UPLOADED`.

### POST /api/projects/:id/consent

Record user consent for AI generation.

**Response:**
```json
{
  "success": true,
  "data": { /* updated project object with consentGiven: true */ }
}
```

### POST /api/projects/:id/generate

Request AI generation (mock candidates via `picsum.photos` in current implementation).

**Request:**
```json
{
  "customPrompt": "optional custom prompt",
  "adjustmentPrompt": "optional adjustment",
  "isRegeneration": false
}
```

**Response:** `202 Accepted` — project object with `status: GENERATING`.

**Rules:**
- Requires a verified purchase (`preview_pack` or `full_pack`).
- Requires `consentGiven: true`.
- Allowed from statuses: `UPLOADED`, `PREVIEW_READY`, `COMPLETED`, `FAILED`.
- Regenerations deduct from `regenerationLimit` (only `full_pack` includes 2 regenerations).

### GET /api/projects/:id/status

Check generation status and progress.

**Response:**
```json
{
  "success": true,
  "data": {
    "status": "GENERATING",
    "progress": 42,
    "resultUrl": null,
    "candidateUrls": null,
    "regenerationRemaining": 2
  }
}
```

### POST /api/projects/:id/select-candidate

Select a candidate image from the generated set.

**Request:**
```json
{
  "index": 0
}
```

**Response:**
- If `purchasedProductId === "full_pack"`: `status` becomes `COMPLETED`, `hdPhotoUrl` is set.
- If `purchasedProductId === "preview_pack"`: `status` becomes `PURCHASED`, HD requires separate `hd_unlock`.

### DELETE /api/projects/:id

Delete a project and its associated data.

**Response:**
```json
{
  "success": true,
  "message": "Project deleted"
}
```

---

## Purchases

All purchase endpoints require authentication.

### POST /api/purchases

Create a purchase record (status `PENDING`, no entitlements granted yet).

**Request:**
```json
{
  "projectId": "project_123",
  "productId": "preview_pack",
  "purchaseToken": "google_purchase_token"
}
```

**`productId` values:** `preview_pack`, `hd_unlock`, `full_pack`

- `preview_pack` — 低清水印预览生成
- `hd_unlock` — 高清合照解锁
- `full_pack` — 高清合照 + PDF 纪念册 + 翻页视频包

**Response:** `201 Created` — Purchase object.

### POST /api/purchases/verify

Verify a purchase and grant entitlements only on success.

**Request:**
```json
{
  "purchaseId": "purchase_123"
}
```

**Response:** Purchase object with `status: VERIFIED` or `status: FAILED`.

**Verification flow:**
1. Tries Google Play Developer API first.
2. Falls back to strong mock validation if Google Play API is not configured.
3. On success, updates the project with `purchasedProductId`, `regenerationLimit`, and/or `hdPhotoUrl`.

---

## Prompts

### POST /api/prompts/optimize

Build a GPT Image 2 prompt from user inputs.

**Request:**
```json
{
  "activityType": "family gathering",
  "personTypes": ["absent loved one", "family member"],
  "style": "NATURAL_FAMILY",
  "eventContext": "optional context",
  "mood": "warm",
  "compositionPrefs": "optional preferences"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "optimizedPrompt": "A warm natural family portrait of...",
    "negativePrompt": "blurry, distorted...",
    "stylePrompt": "natural lighting, soft focus...",
    "safetyNotes": ["AI-generated family memory content"],
    "modelParams": {
      "size": "1024x1536",
      "quality": "high",
      "style": "vivid"
    }
  }
}
```

---

## Albums

All album endpoints require authentication.

### POST /api/albums

Create a new memory album.

**Request:**
```json
{
  "title": "Summer Trip 2026",
  "projectIds": ["project_123", "project_124"],
  "theme": "TRAVEL_MEMORY"
}
```

**Response:** `201 Created` — Album object.

### POST /api/albums/:id/render

Create a PDF + MP4 generation task for the album.

**Request:**
```json
{
  "outputFormat": "pdf+mp4",
  "includePageFlip": true
}
```

**Response:** `202 Accepted` — Album object with `status: RENDERING`.

### GET /api/albums/:id/status

Check album export status and download URLs.

**Response:**
```json
{
  "success": true,
  "data": {
    "status": "RENDERING",
    "progress": 35,
    "pdfUrl": null,
    "mp4Url": null
  }
}
```

---

## Contact

### POST /api/contact

Submit feedback, report, or deletion request. Authentication is optional.

**Request:**
```json
{
  "type": "feedback",
  "email": "user@example.com",
  "message": "Your message here (min 10 characters)",
  "projectId": "optional_project_id"
}
```

**`type` values:** `feedback`, `report`, `deletion`

**Response:**
```json
{
  "success": true,
  "message": "Your message has been received. We will respond within 48 hours."
}
```

---

## Health

### GET /api/health

Liveness check for Railway and container platforms. No authentication required.

**Response:**
```json
{
  "success": true,
  "data": {
    "status": "healthy",
    "uptime": 123.45,
    "timestamp": "2026-05-03T10:35:51Z",
    "env": {
      "databaseUrlConfigured": true,
      "nodeEnv": "production",
      "port": 8080,
      "useMockAuth": false
    }
  }
}
```

---

## Project Status Enum

| Status | Description |
|--------|-------------|
| `DRAFT` | Project created, photos not yet uploaded |
| `UPLOADED` | Both base and person photos uploaded |
| `GENERATING` | AI generation in progress |
| `PREVIEW_READY` | Candidates generated, awaiting selection |
| `PURCHASED` | Preview pack purchased, candidate selected, awaiting HD unlock |
| `COMPLETED` | Full pack or HD unlocked, final image ready |
| `FAILED` | Generation failed |

## Error Format

```json
{
  "success": false,
  "error": "Human-readable error message"
}
```

## Common HTTP Status Codes

| Code | Meaning |
|------|---------|
| `400` | Validation error or bad request |
| `401` | Missing or invalid token |
| `402` | Payment required (purchase verification needed) |
| `403` | Access denied or consent/regeneration limit exceeded |
| `404` | Project or purchase not found |
| `500` | Server error |
|
| `500` | Server error |
