import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { AppError } from './errorHandler';
import { User } from '../models/types';
import { store } from '../services/store';
import { env } from '../utils/env';

declare global {
  namespace Express {
    interface Request {
      user?: User;
    }
  }
}

export function verifyToken(token: string): User | null {
  try {
    const decoded = jwt.verify(token, env.JWT_SECRET) as any;
    return {
      id: decoded.sub || decoded.id,
      email: decoded.email,
      name: decoded.name,
      avatarUrl: decoded.avatarUrl,
      createdAt: decoded.createdAt,
    };
  } catch {
    return null;
  }
}

export function signToken(user: User): string {
  return jwt.sign(
    {
      sub: user.id,
      email: user.email,
      name: user.name,
      avatarUrl: user.avatarUrl,
      createdAt: user.createdAt,
    },
    env.JWT_SECRET,
    { expiresIn: '7d' }
  );
}

// Main auth middleware
export async function authMiddleware(req: Request, _res: Response, next: NextFunction): Promise<void> {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw new AppError(401, 'Authentication required');
  }

  const token = authHeader.substring(7);

  // Mock auth mode (development only)
  if (env.USE_MOCK_AUTH) {
    if (token.length < 10) {
      throw new AppError(401, 'Invalid token');
    }
    req.user = {
      id: 'mock-user-' + token.substring(0, 8),
      email: 'user@example.com',
      name: 'Test User',
      createdAt: new Date().toISOString(),
    };
    next();
    return;
  }

  // JWT verification (production)
  const user = verifyToken(token);
  if (!user) {
    throw new AppError(401, 'Invalid or expired token');
  }

  // Ensure user exists in database
  const dbUser = await store.getUser(user.id);
  if (!dbUser) {
    throw new AppError(401, 'User not found');
  }

  req.user = dbUser;
  next();
}

// Optional auth - doesn't fail if no token
export async function optionalAuth(req: Request, _res: Response, next: NextFunction): Promise<void> {
  const authHeader = req.headers.authorization;

  if (!authHeader?.startsWith('Bearer ')) {
    next();
    return;
  }

  const token = authHeader.substring(7);

  if (env.USE_MOCK_AUTH) {
    if (token.length >= 10) {
      req.user = {
        id: 'mock-user-' + token.substring(0, 8),
        email: 'user@example.com',
        name: 'Test User',
        createdAt: new Date().toISOString(),
      };
    }
    next();
    return;
  }

  const user = verifyToken(token);
  if (user) {
    const dbUser = await store.getUser(user.id);
    if (dbUser) {
      req.user = dbUser;
    }
  }
  next();
}

// Production safety check
export function assertProductionAuth(): void {
  if (env.NODE_ENV === 'production' && env.USE_MOCK_AUTH) {
    console.error('❌ FATAL: USE_MOCK_AUTH is enabled in production. Aborting startup.');
    console.error('   Set USE_MOCK_AUTH=false and configure JWT_SECRET before starting.');
    process.exit(1);
  }
}
