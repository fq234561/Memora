import { Router, Request, Response } from 'express';
import { ApiResponse } from '../models/types';
import { PromptOptimizeRequest, OptimizedPromptResult } from '../models/prompt';
import { promptOptimizer } from '../services/promptOptimizer';
import { AppError } from '../middleware/errorHandler';
import { authMiddleware } from '../middleware/auth';
import { validateBody } from '../middleware/validator';

const router = Router();

router.use(authMiddleware);

// POST /api/prompts/optimize - Build an image generation prompt from user inputs
router.post(
  '/optimize',
  validateBody(['style']),
  (req: Request, res: Response) => {
    const body = req.body as PromptOptimizeRequest;

    const result = promptOptimizer.build(body);

    const response: ApiResponse<OptimizedPromptResult> = {
      success: true,
      data: result,
    };

    res.status(200).json(response);
  }
);

export default router;
