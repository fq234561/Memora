import { Router, Request, Response } from 'express';
import { ApiResponse } from '../models/types';
import { store } from '../services/mockStore';

const router = Router();

// GET /api/health - Health check
router.get('/', (_req: Request, res: Response) => {
  const stats = store.getStats();

  const response: ApiResponse<{ status: string; uptime: number; stats: typeof stats }> = {
    success: true,
    data: {
      status: 'healthy',
      uptime: process.uptime(),
      stats,
    },
  };

  res.status(200).json(response);
});

export default router;
