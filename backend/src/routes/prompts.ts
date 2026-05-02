import { Router, Request, Response } from 'express';
import { ApiResponse } from '../models/types';
import { PromptOptimizeRequest, OptimizedPromptResult } from '../models/prompt';
import { promptOptimizer } from '../services/promptOptimizer';
import { AppError } from '../middleware/errorHandler';
import { mockAuth } from '../middleware/auth';
import { validateBody } from '../middleware/validator';

const router = Router();

router.use(mockAuth);

// POST /api/prompts/optimize — Build a GPT Image 2 prompt from user inputs
router.post(
  '/optimize',
  validateBody(['relationship', 'photoType', 'style']),
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
