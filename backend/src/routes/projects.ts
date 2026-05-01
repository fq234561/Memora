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
} from '../models/types';
import { store } from '../services/mockStore';
import { AppError } from '../middleware/errorHandler';
import { mockAuth } from '../middleware/auth';
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
router.use(mockAuth);

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
router.get('/', (_req: Request, res: Response) => {
  const userId = _req.user!.id;
  const projects = store.getProjectsByUser(userId);

  const response: ApiResponse<Project[]> = {
    success: true,
    data: projects,
  };

  res.status(200).json(response);
});

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
    data: project,
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

  // Build public URL for the uploaded file
  const fileUrl = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;

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

  const project = store.getProject(id);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }
  if (project.status !== ProjectStatus.UPLOADED && project.status !== ProjectStatus.FAILED) {
    throw new AppError(400, 'Project not ready for generation');
  }

  store.updateProject(id, { status: ProjectStatus.GENERATING });

  // Simulate async generation
  setTimeout(() => {
    const success = Math.random() > 0.1; // 90% success rate
    if (success) {
      store.updateProject(id, {
        status: ProjectStatus.PREVIEW_READY,
        generatedPhotoUrl: `https://mock-storage.example.com/preview/${id}_watermarked.jpg`,
      });
    } else {
      store.updateProject(id, { status: ProjectStatus.FAILED });
    }
  }, 5000);

  const response: ApiResponse<Project> = {
    success: true,
    data: store.getProject(id)!,
  };

  res.status(202).json(response);
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
