import { createApp } from './app';
import { env } from './utils/env';
import { initDatabase } from './services/db';
import { assertProductionAuth } from './middleware/auth';

// Production safety check
assertProductionAuth();

// Initialize SQLite database
initDatabase();
console.log('📦 SQLite database initialized');

const app = createApp();

const server = app.listen(env.PORT, () => {
  console.log(`🚀 Server running on port ${env.PORT} in ${env.NODE_ENV} mode`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully');
  server.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  console.log('SIGINT received, shutting down gracefully');
  server.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});
