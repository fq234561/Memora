import { Router, Request, Response } from 'express';
import path from 'path';
import fs from 'fs';
import { authMiddleware } from '../middleware/auth';
import { AppError } from '../middleware/errorHandler';

const router = Router();

// GET /api/uploads/:filename - Serve uploaded files with authentication
router.get('/:filename', authMiddleware, (req: Request, res: Response) => {
  const filename = req.params.filename as string;

  // Security: prevent directory traversal
  if (filename.includes('..') || filename.includes('/')) {
    throw new AppError(400, 'Invalid filename');
  }

  const uploadDir = path.join(process.cwd(), 'uploads');
  const filePath = path.join(uploadDir, filename);

  // Ensure file exists and is within uploads directory
  if (!fs.existsSync(filePath)) {
    throw new AppError(404, 'File not found');
  }

  const resolvedPath = path.resolve(filePath);
  const resolvedUploadDir = path.resolve(uploadDir);
  if (!resolvedPath.startsWith(resolvedUploadDir)) {
    throw new AppError(403, 'Access denied');
  }

  res.sendFile(resolvedPath);
});

export default router;
