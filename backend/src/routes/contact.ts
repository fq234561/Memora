import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { ApiResponse, ContactRequest } from '../models/types';
import { store } from '../services/store';
import { AppError } from '../middleware/errorHandler';
import { optionalAuth } from '../middleware/auth';

const router = Router();

// POST /api/contact - Submit feedback, report, or deletion request
router.post('/', optionalAuth, async (req: Request, res: Response) => {
  const { type, email, message, projectId } = req.body as ContactRequest;

  if (!type || !email || !message) {
    throw new AppError(400, 'Missing required fields: type, email, message');
  }

  if (!['feedback', 'report', 'deletion'].includes(type)) {
    throw new AppError(400, 'Invalid type. Must be feedback, report, or deletion');
  }

  if (message.length < 10) {
    throw new AppError(400, 'Message must be at least 10 characters');
  }

  // Persist to database
  await store.createContactMessage({
    id: uuidv4(),
    userId: req.user?.id,
    type,
    email,
    message,
    metadata: projectId ? { projectId } : undefined,
  });

  const response: ApiResponse = {
    success: true,
    message: 'Your message has been received. We will respond within 48 hours.',
  };

  res.status(200).json(response);
});

export default router;
