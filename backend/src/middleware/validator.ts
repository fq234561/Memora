import { Request, Response, NextFunction } from 'express';
import { AppError } from './errorHandler';

export function validateBody(requiredFields: string[]) {
  return (req: Request, _res: Response, next: NextFunction): void => {
    const missing = requiredFields.filter((field) => {
      const value = req.body[field];
      return value === undefined || value === null || value === '';
    });

    if (missing.length > 0) {
      throw new AppError(400, `Missing required fields: ${missing.join(', ')}`);
    }

    next();
  };
}
