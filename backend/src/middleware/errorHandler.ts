import { Request, Response, NextFunction } from 'express';
import { ApiResponse } from '../models/types';

export class AppError extends Error {
  constructor(
    public statusCode: number,
    message: string,
    public isOperational = true
  ) {
    super(message);
    Object.setPrototypeOf(this, AppError.prototype);
  }
}

export function errorHandler(
  err: Error | AppError,
  _req: Request,
  res: Response,
  _next: NextFunction
): void {
  if (err instanceof AppError) {
    const response: ApiResponse = {
      success: false,
      error: err.message,
    };
    res.status(err.statusCode).json(response);
    return;
  }

  console.error('Unhandled error:', err);
  const response: ApiResponse = {
    success: false,
    error: 'Internal server error',
  };
  res.status(500).json(response);
}

export function notFoundHandler(
  _req: Request,
  res: Response,
  _next: NextFunction
): void {
  const response: ApiResponse = {
    success: false,
    error: 'Resource not found',
  };
  res.status(404).json(response);
}
