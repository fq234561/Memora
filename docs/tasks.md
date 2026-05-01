# Development Milestones

## Legend
- [ ] Pending
- [~] In Progress
- [x] Done

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
- [x] Upload confirmation flow (POST /api/projects/:id/confirm-upload)
- [x] Loading/error states in ViewModel + Screen
- [x] Build check - BUILD SUCCESSFUL
- [x] End-to-end flow verified (Login → Create Project → Photo Selection)

## Phase 6: Image Generation Service (Framework)
- [ ] OpenAI client wrapper
- [ ] Generation job state machine
- [ ] Watermark logic framework
- [ ] HD asset access control
- [ ] Mock generation endpoint

## Phase 7: Google Play Billing (Framework)
- [ ] Android Billing Library integration
- [ ] Backend token verification endpoint
- [ ] Idempotent order processing
- [ ] Entitlement model

## Phase 8: Video Renderer (Framework)
- [ ] Remotion project setup
- [ ] Composition template
- [ ] FFmpeg render pipeline
- [ ] CLI invocation

## Phase 9: Compliance & Polish
- [ ] Data deletion flow
- [ ] Report/feedback flow
- [ ] AI-generated labels
- [ ] Privacy policy draft
- [ ] Play Console notes
