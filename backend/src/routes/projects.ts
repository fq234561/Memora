import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import multer from 'multer';
import {
  ApiResponse,
  CreateProjectRequest,
  Project,
  ProjectStatus,
  StatusResponse,
  GenerationHistoryEntry,
} from '../models/types';
import { store } from '../services/store';
import { uploadFile, getSignedDownloadUrl } from '../services/storage';
import { imageGeneration } from '../services/imageGeneration';
import { AppError } from '../middleware/errorHandler';
import { authMiddleware } from '../middleware/auth';
import { validateBody } from '../middleware/validator';

const router = Router();

// Multer memory storage for R2 upload
const upload = multer({
  storage: multer.memoryStorage(),
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

// Helper: resolve photo key to signed URL (async)
async function resolvePhotoUrl(key: string | undefined): Promise<string | undefined> {
  if (!key) return undefined;
  if (key.startsWith('http')) return key;
  return getSignedDownloadUrl(key);
}

// Helper: add signed URLs to project response
async function resolveProjectUrls(project: Project): Promise<Project> {
  const [deceasedPhotoUrl, livingPhotoUrl, generatedPhotoUrl, hdPhotoUrl] = await Promise.all([
    resolvePhotoUrl(project.deceasedPhotoUrl),
    resolvePhotoUrl(project.livingPhotoUrl),
    resolvePhotoUrl(project.generatedPhotoUrl),
    resolvePhotoUrl(project.hdPhotoUrl),
  ]);

  const candidateUrls = project.candidateUrls
    ? (await Promise.all(project.candidateUrls.map((k) => resolvePhotoUrl(k))))
        .filter((u): u is string => u !== undefined)
    : undefined;

  return {
    ...project,
    deceasedPhotoUrl,
    livingPhotoUrl,
    generatedPhotoUrl,
    hdPhotoUrl,
    candidateUrls,
  };
}

// POST /api/projects - Create a new project
router.post('/', validateBody(['title', 'style']), async (req: Request, res: Response) => {
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

  await store.createProject(project);

  const response: ApiResponse<Project> = {
    success: true,
    data: project,
  };

  res.status(201).json(response);
});

// GET /api/projects - List user's projects
router.get('/', async (req: Request, res: Response) => {
  const userId = req.user!.id;
  const projects = await store.getProjectsByUser(userId);
  const resolvedProjects = await Promise.all(projects.map((p) => resolveProjectUrls(p)));

  const response: ApiResponse<Project[]> = {
    success: true,
    data: resolvedProjects,
  };

  res.status(200).json(response);
});

// GET /api/projects/:id - Get project details
router.get('/:id', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const project = await store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }

  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  const response: ApiResponse<Project> = {
    success: true,
    data: await resolveProjectUrls(project),
  };

  res.status(200).json(response);
});

// POST /api/projects/:id/upload - Upload photo file to R2
router.post('/:id/upload', upload.single('photo'), async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const { type } = req.body as { type?: string };
  const userId = req.user!.id;

  const project = await store.getProject(id);
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

  // Upload to R2
  const timestamp = Date.now();
  const ext = req.file.mimetype === 'image/png' ? '.png' : req.file.mimetype === 'image/webp' ? '.webp' : '.jpg';
  const key = `raw/${userId}/${id}/${type}_${timestamp}_${uuidv4().substring(0, 8)}${ext}`;

  await uploadFile(key, req.file.buffer, req.file.mimetype);

  const updates: Partial<Project> = {};
  if (type === 'deceased') {
    updates.deceasedPhotoUrl = key;
  } else {
    updates.livingPhotoUrl = key;
  }

  // Auto-update status if both photos uploaded
  const currentProject = await store.getProject(id);
  if (!currentProject) {
    throw new AppError(404, 'Project not found');
  }
  const hasDeceased = type === 'deceased' ? true : !!currentProject.deceasedPhotoUrl;
  const hasLiving = type === 'living' ? true : !!currentProject.livingPhotoUrl;

  if (hasDeceased && hasLiving) {
    updates.status = ProjectStatus.UPLOADED;
  }

  await store.updateProject(id, updates);

  const updatedProject = await store.getProject(id);

  const response: ApiResponse<{ project: Project }> = {
    success: true,
    data: {
      project: updatedProject ? await resolveProjectUrls(updatedProject) : currentProject,
    },
  };

  res.status(200).json(response);
});

// POST /api/projects/:id/generate - Request AI generation
router.post('/:id/generate', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;
  const { customPrompt, adjustmentPrompt, isRegeneration } = req.body as {
    customPrompt?: string;
    adjustmentPrompt?: string;
    isRegeneration?: boolean;
  };

  const project = await store.getProject(id);
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
  await store.updateProject(id, updates);

  // Simulate async generation
  setTimeout(async () => {
    const success = Math.random() > 0.1; // 90% success rate
    if (success) {
      try {
        const result = await imageGeneration.generateCandidates({
          projectId: id,
          style: project.style,
          customPrompt,
          adjustmentPrompt,
          deceasedPhotoUrl: project.deceasedPhotoUrl,
          livingPhotoUrl: project.livingPhotoUrl,
          isRegeneration,
        });

        const historyEntry: GenerationHistoryEntry = {
          id: uuidv4(),
          type: isRegeneration ? 'regenerate' : 'initial',
          timestamp: new Date().toISOString(),
          prompt: result.prompt,
          adjustmentPrompt: adjustmentPrompt || undefined,
          candidateUrls: result.candidateUrls,
          status: 'success',
        };

        const successUpdates: Partial<Project> = {
          status: ProjectStatus.PREVIEW_READY,
          candidateUrls: result.candidateUrls,
        };

        // Increment regeneration count only on successful regeneration
        if (isRegeneration) {
          successUpdates.regenerationCount = project.regenerationCount + 1;
        }

        // Append to generation history
        const updatedHistory = [...project.generationHistory, historyEntry];
        (successUpdates as any).generationHistory = updatedHistory;

        await store.updateProject(id, successUpdates);
      } catch (err) {
        console.error('[projects] Image generation provider failed:', err);
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
        await store.updateProject(id, rollbackUpdates);
      }
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
      await store.updateProject(id, rollbackUpdates);
    }
  }, 5000);

  const currentProject = await store.getProject(id);

  const response: ApiResponse<Project> = {
    success: true,
    data: currentProject ? await resolveProjectUrls(currentProject) : project,
  };

  res.status(202).json(response);
});

// POST /api/projects/:id/select-candidate - Select a candidate image
router.post('/:id/select-candidate', validateBody(['index']), async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;
  const { index } = req.body as { index: number };

  const project = await store.getProject(id);
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

  await store.updateProject(id, updates);

  const updatedProject = await store.getProject(id);

  const response: ApiResponse<Project> = {
    success: true,
    data: updatedProject ? await resolveProjectUrls(updatedProject) : project,
  };

  res.status(200).json(response);
});

// GET /api/projects/:id/status - Check generation status
router.get('/:id/status', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const project = await store.getProject(id);
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
    resultUrl: await resolvePhotoUrl(project.generatedPhotoUrl),
    candidateUrls: project.candidateUrls
      ? (await Promise.all(project.candidateUrls.map((k) => resolvePhotoUrl(k))))
          .filter((u): u is string => u !== undefined)
      : undefined,
    regenerationRemaining: Math.max(0, project.regenerationLimit - project.regenerationCount),
  };

  const response: ApiResponse<StatusResponse> = {
    success: true,
    data,
  };

  res.status(200).json(response);
});

// POST /api/projects/:id/consent - Record consent
router.post('/:id/consent', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const project = await store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  await store.updateProject(id, { consentGiven: true });

  const updatedProject = await store.getProject(id);

  const response: ApiResponse<Project> = {
    success: true,
    data: updatedProject ? await resolveProjectUrls(updatedProject) : project,
  };

  res.status(200).json(response);
});

// DELETE /api/projects/:id - Delete project
router.delete('/:id', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const project = await store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  await store.deleteProject(id);

  const response: ApiResponse = {
    success: true,
    message: 'Project deleted',
  };

  res.status(200).json(response);
});

export default router;
