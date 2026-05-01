import { Request, Response, NextFunction } from 'express';
import { AppError } from './errorHandler';
import { User } from '../models/types';

declare global {
  namespace Express {
    interface Request {
      user?: User;
    }
  }
}

// Mock auth middleware - validates Bearer token and attaches mock user
// In production, this verifies Google ID tokens
export function mockAuth(req: Request, _res: Response, next: NextFunction): void {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw new AppError(401, 'Authentication required');
  }

  const token = authHeader.substring(7);

  // Mock token validation - in production verify with Google
  if (token.length < 10) {
    throw new AppError(401, 'Invalid token');
  }

  // Attach mock user
  req.user = {
    id: 'mock-user-' + token.substring(0, 8),
    email: 'user@example.com',
    name: 'Test User',
    createdAt: new Date().toISOString(),
  };

  next();
}

// Optional auth - doesn't fail if no token
export function optionalAuth(req: Request, _res: Response, next: NextFunction): void {
  const authHeader = req.headers.authorization;

  if (authHeader?.startsWith('Bearer ')) {
    const token = authHeader.substring(7);
    req.user = {
      id: 'mock-user-' + token.substring(0, 8),
      email: 'user@example.com',
      name: 'Test User',
      createdAt: new Date().toISOString(),
    };
  }

  next();
}
