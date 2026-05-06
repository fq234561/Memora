import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import {
  ApiResponse,
  Album,
  AlbumStatus,
  CreateAlbumRequest,
} from '../models/types';
import { store } from '../services/store';
import { AppError } from '../middleware/errorHandler';
import { authMiddleware } from '../middleware/auth';
import { validateBody } from '../middleware/validator';

const router = Router();

router.use(authMiddleware);

// POST /api/albums - Create a new album
router.post('/', validateBody(['title', 'projectIds']), async (req: Request, res: Response) => {
  const { title, projectIds } = req.body as CreateAlbumRequest;
  const userId = req.user!.id;

  if (!Array.isArray(projectIds) || projectIds.length === 0) {
    throw new AppError(400, 'projectIds must be a non-empty array');
  }

  // Verify all projects belong to the user
  for (const projectId of projectIds) {
    const project = await store.getProject(projectId);
    if (!project) {
      throw new AppError(404, `Project not found: ${projectId}`);
    }
    if (project.userId !== userId) {
      throw new AppError(403, `Access denied to project: ${projectId}`);
    }
  }

  const album: Album = {
    id: uuidv4(),
    userId,
    title,
    projectIds,
    status: AlbumStatus.DRAFT,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  await store.createAlbum(album);

  // Link projects to album
  for (const projectId of projectIds) {
    await store.updateProject(projectId, { albumId: album.id });
  }

  const response: ApiResponse<Album> = {
    success: true,
    data: album,
  };

  res.status(201).json(response);
});

// GET /api/albums - List user's albums
router.get('/', async (req: Request, res: Response) => {
  const userId = req.user!.id;
  const albums = await store.getAlbumsByUser(userId);

  const response: ApiResponse<Album[]> = {
    success: true,
    data: albums,
  };

  res.status(200).json(response);
});

// GET /api/albums/:id - Get album details
router.get('/:id', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const album = await store.getAlbum(id);
  if (!album) {
    throw new AppError(404, 'Album not found');
  }

  if (album.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  const response: ApiResponse<Album> = {
    success: true,
    data: album,
  };

  res.status(200).json(response);
});

// POST /api/albums/:id/render - Request album rendering
router.post('/:id/render', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const album = await store.getAlbum(id);
  if (!album) {
    throw new AppError(404, 'Album not found');
  }
  if (album.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  if (album.status === AlbumStatus.RENDERING) {
    throw new AppError(400, 'Album is already rendering');
  }

  // Set rendering status
  await store.updateAlbum(id, { status: AlbumStatus.RENDERING });

  // Mock async render: set READY after 10 seconds with mock URLs
  setTimeout(async () => {
    try {
      await store.updateAlbum(id, {
        status: AlbumStatus.READY,
        pdfUrl: `https://picsum.photos/seed/${id}_pdf/800/1200`,
        mp4Url: `https://picsum.photos/seed/${id}_mp4/800/1200`,
      });
    } catch (err) {
      console.error('[albums] Mock render failed:', err);
      await store.updateAlbum(id, { status: AlbumStatus.FAILED });
    }
  }, 10000);

  const updatedAlbum = await store.getAlbum(id);

  const response: ApiResponse<Album> = {
    success: true,
    data: updatedAlbum || album,
  };

  res.status(202).json(response);
});

// GET /api/albums/:id/status - Check render status
router.get('/:id/status', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const album = await store.getAlbum(id);
  if (!album) {
    throw new AppError(404, 'Album not found');
  }
  if (album.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  let progress: number | undefined;
  if (album.status === AlbumStatus.RENDERING) {
    progress = Math.floor(Math.random() * 80) + 10;
  }

  const response: ApiResponse<{ status: AlbumStatus; progress?: number; pdfUrl?: string; mp4Url?: string }> = {
    success: true,
    data: {
      status: album.status,
      progress,
      pdfUrl: album.pdfUrl,
      mp4Url: album.mp4Url,
    },
  };

  res.status(200).json(response);
});

// DELETE /api/albums/:id - Delete album
router.delete('/:id', async (req: Request, res: Response) => {
  const id = req.params.id as string;
  const userId = req.user!.id;

  const album = await store.getAlbum(id);
  if (!album) {
    throw new AppError(404, 'Album not found');
  }
  if (album.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  // Unlink projects from album
  for (const projectId of album.projectIds) {
    const project = await store.getProject(projectId);
    if (project && project.userId === userId) {
      await store.updateProject(projectId, { albumId: null });
    }
  }

  await store.deleteAlbum(id);

  const response: ApiResponse = {
    success: true,
    message: 'Album deleted',
  };

  res.status(200).json(response);
});

export default router;
