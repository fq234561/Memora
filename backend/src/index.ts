import { createApp } from './app';
import { env } from './utils/env';
import { initDatabase, closeDatabase } from './services/db';
import { assertProductionAuth } from './middleware/auth';

async function main() {
  // Production safety check
  assertProductionAuth();

  const app = createApp();

  const server = app.listen(env.PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${env.PORT} in ${env.NODE_ENV} mode`);
  });

  // Let Railway's health check confirm the HTTP process first.
  // Database startup errors are still visible in Deploy Logs.
  initDatabase()
    .then(() => {
      console.log('Postgres database initialized');
    })
    .catch((err) => {
      console.error('Postgres database initialization failed:', err);
    });

  // Graceful shutdown
  process.on('SIGTERM', async () => {
    console.log('SIGTERM received, shutting down gracefully');
    server.close(async () => {
      await closeDatabase();
      console.log('Server closed');
      process.exit(0);
    });
  });

  process.on('SIGINT', async () => {
    console.log('SIGINT received, shutting down gracefully');
    server.close(async () => {
      await closeDatabase();
      console.log('Server closed');
      process.exit(0);
    });
  });
}

main().catch((err) => {
  console.error('Failed to start server:', err);
  process.exit(1);
});
