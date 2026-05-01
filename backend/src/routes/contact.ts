import { Router, Request, Response } from 'express';
import { ApiResponse, ContactRequest } from '../models/types';
import { AppError } from '../middleware/errorHandler';
import { optionalAuth } from '../middleware/auth';

const router = Router();

// POST /api/contact - Submit feedback, report, or deletion request
router.post('/', optionalAuth, (req: Request, res: Response) => {
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

  // In production: send email notification, store in DB, or create ticket
  console.log(`[${type.toUpperCase()}] From: ${email}${projectId ? ` | Project: ${projectId}` : ''}`);
  console.log(`Message: ${message.substring(0, 200)}${message.length > 200 ? '...' : ''}`);

  const response: ApiResponse = {
    success: true,
    message: 'Your message has been received. We will respond within 48 hours.',
  };

  res.status(200).json(response);
});

export default router;
