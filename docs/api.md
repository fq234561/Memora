# API Contract

## Base URL
```
Development: http://localhost:3000
Production:  (to be configured)
```

## Authentication
All endpoints except `/auth/google` require header:
```
Authorization: Bearer <app_session_token>
```

---

### POST /auth/google
Authenticate with Google ID token and receive app session.

**Request:**
```json
{
  "idToken": "<google_id_token>"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "sessionToken": "<app_session_jwt>",
    "user": {
      "id": "user_123",
      "email": "user@example.com",
      "displayName": "User Name"
    }
  }
}
```

---

### POST /projects
Create a new memorial project.

**Request:**
```json
{
  "title": "For Mom",
  "style": "natural_family_photo",
  "locale": "en-US"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "project_123",
    "title": "For Mom",
    "style": "natural_family_photo",
    "status": "draft",
    "createdAt": "2026-04-30T12:00:00Z"
  }
}
```

---

### POST /uploads/sign
Get signed URL for uploading a photo.

**Request:**
```json
{
  "projectId": "project_123",
  "assetType": "deceased_reference",
  "mimeType": "image/jpeg",
  "fileSize": 2048000
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "uploadUrl": "https://signed-url...",
    "assetId": "asset_456",
    "expiresAt": "2026-04-30T12:15:00Z"
  }
}
```

---

### POST /projects/:id/preview
Request preview image generation.

**Request:**
```json
{
  "consentVersion": "v1",
  "consentText": "I have the right..."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "jobId": "job_789",
    "status": "queued"
  }
}
```

---

### GET /projects/:id
Get project details and status.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "project_123",
    "title": "For Mom",
    "style": "natural_family_photo",
    "status": "preview_ready",
    "assets": [
      {
        "id": "asset_456",
        "type": "deceased_reference",
        "url": "https://...",
        "width": 1024,
        "height": 1024
      }
    ],
    "previews": [
      {
        "id": "preview_1",
        "url": "https://...",
        "width": 512,
        "height": 512
      }
    ],
    "finalImage": null,
    "finalVideo": null,
    "order": null,
    "createdAt": "2026-04-30T12:00:00Z"
  }
}
```

---

### POST /billing/google/verify
Verify Google Play purchase token.

**Request:**
```json
{
  "projectId": "project_123",
  "productId": "memorial_photo_video_1",
  "purchaseToken": "google_purchase_token"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "verified": true,
    "entitlement": "photo_and_video",
    "projectId": "project_123"
  }
}
```

---

### POST /projects/:id/finalize
Generate HD photo and optional video (paid only).

**Response:**
```json
{
  "success": true,
  "data": {
    "jobId": "job_999",
    "status": "queued"
  }
}
```

---

### DELETE /projects/:id
Delete project and all assets.

**Response:**
```json
{
  "success": true,
  "data": {
    "deleted": true
  }
}
```

---

### POST /reports
Submit feedback or report.

**Request:**
```json
{
  "projectId": "project_123",
  "reason": "inappropriate_content",
  "message": "Description of issue..."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "reportId": "report_111"
  }
}
```

---

## Error Format
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired session token"
  }
}
```

## Common Error Codes
- `UNAUTHORIZED` - Invalid/missing session token
- `FORBIDDEN` - User cannot access this resource
- `NOT_FOUND` - Project or asset not found
- `VALIDATION_ERROR` - Invalid request data
- `PAYMENT_REQUIRED` - HD assets require verified purchase
- `GENERATION_FAILED` - Image/video generation failed
