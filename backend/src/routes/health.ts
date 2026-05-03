import { Router, Request, Response } from 'express';
import { ApiResponse } from '../models/types';
import { env } from '../utils/env';

const router = Router();

// GET /api/health - liveness check for Railway and container platforms.
router.get('/', (_req: Request, res: Response) => {
  const response: ApiResponse<{
    status: string;
    uptime: number;
    timestamp: string;
    env: {
      databaseUrlConfigured: boolean;
      nodeEnv: string;
      port: number;
      useMockAuth: boolean;
    };
  }> = {
    success: true,
    data: {
      status: 'healthy',
      uptime: process.uptime(),
      timestamp: new Date().toISOString(),
      env: {
        databaseUrlConfigured: Boolean(env.DATABASE_URL),
        nodeEnv: env.NODE_ENV,
        port: env.PORT,
        useMockAuth: env.USE_MOCK_AUTH,
      },
    },
  };

  res.status(200).json(response);
});

export default router;
