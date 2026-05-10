import './instrument';
import express, { Application } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import * as Sentry from '@sentry/node';
import { env } from './utils/env';
import { errorHandler, notFoundHandler } from './middleware/errorHandler';

// Routes
import authRoutes from './routes/auth';
import projectRoutes from './routes/projects';
import purchaseRoutes from './routes/purchases';
import contactRoutes from './routes/contact';
import healthRoutes from './routes/health';
import promptRoutes from './routes/prompts';
import albumRoutes from './routes/albums';
import stripeRoutes, { stripeWebhookHandler } from './routes/stripe';

export function createApp(): Application {
  const app = express();

  // Security middleware
  app.use(helmet());
  app.use(cors({
    origin: env.ALLOWED_ORIGINS,
    credentials: true,
  }));

  // Logging
  app.use(morgan(env.NODE_ENV === 'development' ? 'dev' : 'combined'));

  // Stripe webhook needs raw body BEFORE express.json()
  app.post('/api/stripe/webhook', express.raw({ type: 'application/json' }), stripeWebhookHandler);

  // Body parsing
  app.use(express.json({ limit: '10mb' }));
  app.use(express.urlencoded({ extended: true, limit: '10mb' }));

  // API routes
  app.use('/api/auth', authRoutes);
  app.use('/api/projects', projectRoutes);
  app.use('/api/purchases', purchaseRoutes);
  app.use('/api/contact', contactRoutes);
  app.use('/api/health', healthRoutes);
  app.use('/api/prompts', promptRoutes);
  app.use('/api/albums', albumRoutes);
  app.use('/api/stripe', stripeRoutes);

  // Root endpoint
  app.get('/', (_req, res) => {
    res.json({
      name: 'Family Memory Photo API',
      version: '1.0.0',
      environment: env.NODE_ENV,
      documentation: '/api/health',
    });
  });

  // Error handling
  app.use(notFoundHandler);
  Sentry.setupExpressErrorHandler(app);
  app.use(errorHandler);

  return app;
}
