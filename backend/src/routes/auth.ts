import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { OAuth2Client } from 'google-auth-library';
import { ApiResponse, AuthRequest } from '../models/types';
import { store } from '../services/store';
import { AppError } from '../middleware/errorHandler';
import { validateBody } from '../middleware/validator';
import { signToken } from '../middleware/auth';
import { env } from '../utils/env';

const router = Router();

// POST /api/auth/google
router.post('/google', validateBody(['idToken']), async (req: Request, res: Response) => {
  const { idToken } = req.body as AuthRequest;

  let email: string;
  let name: string;
  let googleId: string;

  if (env.USE_MOCK_AUTH) {
    // Mock mode: skip Google verification, trust the idToken as a user identifier
    if (!idToken || idToken.length < 20) {
      throw new AppError(400, 'Invalid ID token');
    }
    googleId = 'mock-google-' + idToken.substring(0, 16);
    email = `user_${googleId.substring(0, 8)}@example.com`;
    name = 'Family User';
  } else {
    // Real Google Sign-In verification
    if (!env.GOOGLE_CLIENT_ID) {
      throw new AppError(500, 'Google OAuth not configured');
    }
    const client = new OAuth2Client(env.GOOGLE_CLIENT_ID);
    try {
      const ticket = await client.verifyIdToken({
        idToken,
        audience: env.GOOGLE_CLIENT_ID,
      });
      const payload = ticket.getPayload();
      if (!payload) {
        throw new AppError(401, 'Invalid Google ID token');
      }
      googleId = payload.sub;
      email = payload.email || `user_${googleId}@example.com`;
      name = payload.name || 'Family User';
    } catch {
      throw new AppError(401, 'Google token verification failed');
    }
  }

  // Find or create user
  let user = await store.getUser(googleId);
  if (!user) {
    // Try by email for existing users
    user = await store.getUserByEmail(email);
  }
  if (!user) {
    user = await store.createUser({
      id: googleId,
      email,
      name,
      createdAt: new Date().toISOString(),
    });
  }

  // Sign JWT
  const accessToken = signToken(user);

  const response: ApiResponse<{ user: typeof user; accessToken: string }> = {
    success: true,
    data: { user, accessToken },
  };

  res.status(200).json(response);
});

export default router;
