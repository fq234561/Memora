import { Router, Request, Response } from 'express';
import { ApiResponse } from '../models/types';

const router = Router();

// GET /api/health - liveness check for Railway and container platforms.
router.get('/', (_req: Request, res: Response) => {
  const response: ApiResponse<{ status: string; uptime: number; timestamp: string }> = {
    success: true,
    data: {
      status: 'healthy',
      uptime: process.uptime(),
      timestamp: new Date().toISOString(),
    },
  };

  res.status(200).json(response);
});

export default router;
