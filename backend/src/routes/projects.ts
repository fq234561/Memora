import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import multer from 'multer';
import path from 'path';
import fs from 'fs';
import {
  ApiResponse,
  CreateProjectRequest,
  Project,
  ProjectStatus,
  UploadUrlResponse,
  StatusResponse,
  GenerationHistoryEntry,
} from '../models/types';
import { store } from '../services/store';
import { AppError } from '../middleware/errorHandler';
import { authMiddleware } from '../middleware/auth';
import { validateBody } from '../middleware/validator';

const router = Router();

// Multer storage configuration
const storage = multer.diskStorage({
  destination: (_req, _file, cb) => {
    const uploadDir = path.join(process.cwd(), 'uploads');
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: (_req, file, cb) => {
    const uniqueName = `${Date.now()}-${uuidv4().substring(0, 8)}${path.extname(file.originalname)}`;
    cb(null, uniqueName);
  },
});

const upload = multer({
  storage,
  limits: { fileSize: 20 * 1024 * 1024 }, // 20MB max
  fileFilter: (_req, file, cb) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (allowedTypes.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Only JPG, PNG, and WebP images are allowed'));
    }
  },
});

// All project routes require authentication
router.use(authMiddleware);

// POST /api/projects - Create a new project
router.post('/', validateBody(['title', 'style']), (req: Request, res: Response) => {
  const { title, style } = req.body as CreateProjectRequest;
  const userId = req.user!.id;

  const project: Project = {
    id: uuidv4(),
    userId,
    title,
    style,
    status: ProjectStatus.DRAFT,
    consentGiven: false,
    regenerationCount: 0,
    regenerationLimit: 0,
    generationHistory: [],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  store.createProject(project);

  const response: ApiResponse<Project> = {
    success: true,
    data: project,
  };

  res.status(201).json(response);
});

// GET /api/projects - List user's projects
router.get('/', (req: Request, res: Response) => {
  const userId = req.user!.id;
  const projects = store.getProjectsByUser(userId).map((p) => resolveProjectUrls(req, p));

  const response: ApiResponse<Project[]> = {
    success: true,
    data: projects,
  };

  res.status(200).json(response);
});

function resolveFileUrl(req: Request, url?: string): string | undefined {
  if (!url) return undefined;
  if (url.startsWith('http')) return url;
  return `${req.protocol}://${req.get('host')}${url}`;
}

function resolveProjectUrls(req: Request, project: Project): Project {
  return {
    ...project,
    deceasedPhotoUrl: resolveFileUrl(req, project.deceasedPhotoUrl),
    livingPhotoUrl: resolveFileUrl(req, project.livingPhotoUrl),
    generatedPhotoUrl: resolveFileUrl(req, project.generatedPhotoUrl),
    hdPhotoUrl: resolveFileUrl(req, project.hdPhotoUrl),
    candidateUrls: project.candidateUrls?.map((u) => resolveFileUrl(req, u)!),
  };
}

// GET /api/projects/:id - Get project details
router.get('/:id', (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const project = store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }

  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  const response: ApiResponse<Project> = {
    success: true,
    data: resolveProjectUrls(req, project),
  };

  res.status(200).json(response);
});

// POST /api/projects/:id/upload - Upload photo file directly
router.post('/:id/upload', upload.single('photo'), (req: Request, res: Response) => {
  const id = req.params.id as string;
  const { type } = req.body as { type?: string };
  const userId = req.user!.id;

  const project = store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  if (!req.file) {
    throw new AppError(400, 'No file uploaded');
  }

  if (!type || !['deceased', 'living'].includes(type)) {
    throw new AppError(400, 'Invalid upload type. Must be "deceased" or "living"');
  }

  // Store relative path for private access via authenticated route
  const fileUrl = `/api/uploads/${req.file.filename}`;

  const updates: Partial<Project> = {};
  if (type === 'deceased') {
    updates.deceasedPhotoUrl = fileUrl;
  } else {
    updates.livingPhotoUrl = fileUrl;
  }

  // Auto-update status if both photos uploaded
  const currentProject = store.getProject(id)!;
  const hasDeceased = type === 'deceased' ? true : !!currentProject.deceasedPhotoUrl;
  const hasLiving = type === 'living' ? true : !!currentProject.livingPhotoUrl;

  if (hasDeceased && hasLiving) {
    updates.status = ProjectStatus.UPLOADED;
  }

  store.updateProject(id, updates);

  const response: ApiResponse<{ url: string; fileName: string }> = {
    success: true,
    data: {
      url: fileUrl,
      fileName: req.file.filename,
    },
  };

  res.status(200).json(response);
});

// POST /api/projects/:id/generate - Request AI generation
router.post('/:id/generate', (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;
  const { customPrompt, adjustmentPrompt, isRegeneration } = req.body as {
    customPrompt?: string;
    adjustmentPrompt?: string;
    isRegeneration?: boolean;
  };

  const project = store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  // Check purchase entitlement
  if (!project.purchasedProductId || !['preview_pack', 'full_pack'].includes(project.purchasedProductId)) {
    throw new AppError(402, 'Purchase required: Preview Pack or Full Pack needed to generate');
  }

  // P0-5: Require user consent before generating
  if (!project.consentGiven) {
    throw new AppError(403, 'User consent required before generation. Please agree to the terms first.');
  }

  // Allow generation from UPLOADED, PREVIEW_READY (regenerate), COMPLETED (regenerate from download), or FAILED
  const allowedStatuses = [
    ProjectStatus.UPLOADED,
    ProjectStatus.PREVIEW_READY,
    ProjectStatus.COMPLETED,
    ProjectStatus.FAILED,
  ];
  if (!allowedStatuses.includes(project.status)) {
    throw new AppError(400, 'Project not ready for generation');
  }

  // Check regeneration quota
  if (isRegeneration) {
    if (project.regenerationCount >= project.regenerationLimit) {
      throw new AppError(403, 'Regeneration limit reached. Purchase additional regenerations to continue.');
    }
  }

  // Save previous state for rollback on failure
  const previousStatus = project.status;
  const previousCandidateUrls = project.candidateUrls;

  const updates: Partial<Project> = { status: ProjectStatus.GENERATING };
  if (customPrompt) {
    (updates as any).customPrompt = customPrompt;
  }
  // Clear previous candidates on new generation
  updates.candidateUrls = undefined;
  updates.selectedCandidateIndex = undefined;
  store.updateProject(id, updates);

  // Simulate async generation
  setTimeout(() => {
    const success = Math.random() > 0.1; // 90% success rate
    if (success) {
      // Generate 4 candidate images with different seeds
      const candidateUrls = [
        `https://picsum.photos/seed/${id}_c1/400/600`,
        `https://picsum.photos/seed/${id}_c2/400/600`,
        `https://picsum.photos/seed/${id}_c3/400/600`,
        `https://picsum.photos/seed/${id}_c4/400/600`,
      ];

      const historyEntry: GenerationHistoryEntry = {
        id: uuidv4(),
        type: isRegeneration ? 'regenerate' : 'initial',
        timestamp: new Date().toISOString(),
        prompt: customPrompt || 'default_prompt',
        adjustmentPrompt: adjustmentPrompt || undefined,
        candidateUrls,
        status: 'success',
      };

      const successUpdates: Partial<Project> = {
        status: ProjectStatus.PREVIEW_READY,
        candidateUrls,
      };

      // Increment regeneration count only on successful regeneration
      if (isRegeneration) {
        successUpdates.regenerationCount = project.regenerationCount + 1;
      }

      // Append to generation history
      const updatedHistory = [...project.generationHistory, historyEntry];
      (successUpdates as any).generationHistory = updatedHistory;

      store.updateProject(id, successUpdates);
    } else {
      // Technical failure: rollback state, do NOT deduct regeneration count
      const rollbackUpdates: Partial<Project> = {
        status: previousCandidateUrls && previousCandidateUrls.length > 0
          ? ProjectStatus.PREVIEW_READY
          : previousStatus === ProjectStatus.COMPLETED
            ? ProjectStatus.UPLOADED
            : previousStatus,
      };
      if (previousCandidateUrls && previousCandidateUrls.length > 0) {
        rollbackUpdates.candidateUrls = previousCandidateUrls;
      }
      store.updateProject(id, rollbackUpdates);
    }
  }, 5000);

  const response: ApiResponse<Project> = {
    success: true,
    data: store.getProject(id)!,
  };

  res.status(202).json(response);
});

// POST /api/projects/:id/select-candidate - Select a candidate image
router.post('/:id/select-candidate', validateBody(['index']), (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;
  const { index } = req.body as { index: number };

  const project = store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }
  if (project.status !== ProjectStatus.PREVIEW_READY) {
    throw new AppError(400, 'No candidates available for selection');
  }
  if (!project.candidateUrls || index < 0 || index >= project.candidateUrls.length) {
    throw new AppError(400, 'Invalid candidate index');
  }

  const selectedUrl = project.candidateUrls[index];
  const updates: Partial<Project> = {
    selectedCandidateIndex: index,
    generatedPhotoUrl: selectedUrl,
  };

  if (project.purchasedProductId === 'full_pack') {
    // Full pack includes HD unlock, complete immediately
    updates.status = ProjectStatus.COMPLETED;
    updates.hdPhotoUrl = `https://picsum.photos/seed/${id}_hd/800/1200`;
  } else {
    // Preview pack requires separate HD unlock purchase
    updates.status = ProjectStatus.PURCHASED;
  }

  store.updateProject(id, updates);

  const response: ApiResponse<Project> = {
    success: true,
    data: store.getProject(id)!,
  };

  res.status(200).json(response);
});

// GET /api/projects/:id/status - Check generation status
router.get('/:id/status', (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const project = store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  let progress: number | undefined;
  if (project.status === ProjectStatus.GENERATING) {
    progress = Math.floor(Math.random() * 80) + 10;
  }

  const data: StatusResponse = {
    status: project.status,
    progress,
    resultUrl: project.generatedPhotoUrl,
    candidateUrls: project.candidateUrls,
    regenerationRemaining: Math.max(0, project.regenerationLimit - project.regenerationCount),
  };

  const response: ApiResponse<StatusResponse> = {
    success: true,
    data,
  };

  res.status(200).json(response);
});

// POST /api/projects/:id/consent - Record consent
router.post('/:id/consent', (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const project = store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  store.updateProject(id, { consentGiven: true });

  const response: ApiResponse<Project> = {
    success: true,
    data: store.getProject(id)!,
  };

  res.status(200).json(response);
});

// DELETE /api/projects/:id - Delete project
router.delete('/:id', (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const project = store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  store.deleteProject(id);

  const response: ApiResponse = {
    success: true,
    message: 'Project deleted',
  };

  res.status(200).json(response);
});

export default router;
