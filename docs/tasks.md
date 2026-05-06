# Development Milestones

## Legend
- [ ] Pending
- [~] In Progress
- [x] Done

## Repository Sync
- [x] Local `main` matches GitHub `origin/main` at `69e8005` (2026-05-05 check)
- [~] Local worktree has an uncommitted Android API base URL change for Railway testing

---

## Phase 1: Project Skeleton & Docs
- [x] Create directory structure (android/, backend/, video-renderer/, docs/)
- [x] Create docs/product-spec.md
- [x] Create docs/api.md
- [x] Create docs/tasks.md
- [x] Create README.md
- [x] Create .env.example files

## Phase 2: Android Basic Shell
- [x] Initialize Android project (Kotlin + Jetpack Compose)
- [x] Setup Navigation Compose
- [x] Create MVVM base structure
- [x] Implement screen placeholders:
  - [x] LoginScreen
  - [x] HomeScreen
  - [x] CreateProjectScreen
  - [x] UploadPhotosScreen
  - [x] StyleSelectionScreen
  - [x] ConsentScreen
  - [x] PreviewScreen
  - [x] PurchaseScreen
  - [x] DownloadScreen
  - [x] SettingsScreen
- [x] Mock data layer
- [x] Build check - BUILD SUCCESSFUL (Debug 8.54MB, Release 5.74MB)

## Phase 3: Photo Picker
- [x] Integrate Android Photo Picker (ActivityResultContracts.PickVisualMedia)
- [x] Image validation (JPG/PNG/WebP, 10KB-20MB, min 256x256)
- [x] Thumbnail display (Coil AsyncImage)
- [x] Snackbar error feedback
- [x] No READ_MEDIA_IMAGES permission required
- [x] Build & run check passed

## Phase 4: Backend Skeleton
- [x] Initialize Node.js + TypeScript project
- [x] Setup Express server
- [x] Implement mock APIs (auth, projects, purchases, contact, health)
- [x] Environment variable configuration
- [x] Type check / test run
- [x] Server startup verification (all endpoints responding)

## Phase 5: Android + Mock API Integration
- [x] Setup Retrofit client (OkHttp + Gson + logging)
- [x] Create DTOs (ApiResponse, ProjectDto, AuthResponse, etc.)
- [x] Repository layer (AuthRepository, ProjectRepository)
- [x] TokenManager (SharedPreferences-based)
- [x] Project creation flow (POST /api/projects)
- [x] Multipart upload flow (POST /api/projects/:id/upload)
- [~] Historical planning docs still mention the earlier signed-upload design
- [x] Loading/error states in ViewModel + Screen
- [x] Build check - BUILD SUCCESSFUL
- [x] End-to-end flow verified (Login -> Create Project -> Photo Selection)

## Phase 6: Image Generation Service (Framework)
- [ ] OpenAI client wrapper
- [x] Prompt optimization endpoint and prompt builder
- [x] Mock generation endpoint with 4 candidate images
- [x] Candidate selection endpoint
- [x] Regeneration quota tracking and generation history
- [~] Generation job state machine (status/progress/polling exists; no persistent queue/worker yet)
- [~] HD asset access control (entitlement checks exist; HD output still mock URL)
- [ ] Watermark logic framework

## Phase 7: Google Play Billing (Framework)
- [ ] Android Billing Library integration
- [x] Backend purchase creation endpoint
- [x] Backend Google Play token verification path with mock fallback
- [x] Idempotent order processing by purchase token
- [x] Basic entitlement model for preview/full/HD unlock
- [~] Android purchase screen wired to backend with mock purchase token

## Phase 8: Video Renderer (Framework)
- [ ] Remotion project setup
- [ ] Composition template
- [ ] FFmpeg render pipeline
- [ ] CLI invocation

## Phase 9: Compliance & Polish
- [~] Data deletion flow (project delete endpoint exists; full asset cleanup/account deletion not complete)
- [~] Report/feedback flow (backend contact endpoint exists; Settings UI action not wired)
- [~] AI-generated labels (consent/product copy exists; output watermark/label rendering not complete)
- [ ] Privacy policy draft
- [ ] Play Console notes

## Phase 10: Classification, Album & Advanced Features
- [x] Activity type and person type tagging
- [x] Project filtering by date/type/person
- [x] Album API (basic)
- [ ] AI auto-recognition for detectedTags (FUTURE)
- [ ] Real PDF/MP4 rendering for albums (FUTURE)
- [ ] Push notification for album rendering completion (FUTURE)

## Current Next Priorities
- [ ] Replace mock image generation with server-side OpenAI image generation
- [ ] Persist generation jobs in a queue/worker instead of in-process `setTimeout`
- [ ] Implement watermarking and AI-generated labels on preview/HD assets
- [ ] Integrate real Android Google Play Billing Library
- [ ] Decide whether to archive or refresh older Chinese planning docs that reference retired API paths
- [ ] Album 数据模型与 API（纪念册路线）
