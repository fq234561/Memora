import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { ApiResponse, AuthRequest } from '../models/types';
import { store } from '../services/mockStore';
import { AppError } from '../middleware/errorHandler';
import { validateBody } from '../middleware/validator';

const router = Router();

// POST /api/auth/google
router.post('/google', validateBody(['idToken']), (req: Request, res: Response) => {
  const { idToken } = req.body as AuthRequest;

  // Mock Google token verification
  // In production: verify with Google OAuth2 API
  if (!idToken || idToken.length < 20) {
    throw new AppError(400, 'Invalid ID token');
  }

  const userId = uuidv4();
  const user = store.createUser({
    id: userId,
    email: `user_${userId.substring(0, 8)}@example.com`,
    name: 'Memorial User',
    createdAt: new Date().toISOString(),
  });

  // Generate mock session token
  const accessToken = `mock_token_${uuidv4().replace(/-/g, '')}`;

  const response: ApiResponse<{ user: typeof user; accessToken: string }> = {
    success: true,
    data: { user, accessToken },
  };

  res.status(200).json(response);
});

export default router;
