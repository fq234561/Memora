import dotenv from 'dotenv';
import path from 'path';

dotenv.config({ path: path.resolve(__dirname, '../../.env') });

function requireEnv(key: string): string {
  const value = process.env[key];
  if (!value) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return value;
}

function optionalEnv(key: string, defaultValue: string): string {
  return process.env[key] || defaultValue;
}

export const env = {
  PORT: parseInt(optionalEnv('PORT', '3000'), 10),
  NODE_ENV: optionalEnv('NODE_ENV', 'development'),

  // Database
  DATABASE_URL: optionalEnv('DATABASE_URL', ''),

  // Storage (R2 / S3-compatible)
  STORAGE_ENDPOINT: optionalEnv('STORAGE_ENDPOINT', ''),
  STORAGE_BUCKET: optionalEnv('STORAGE_BUCKET', 'memorial-assets'),
  STORAGE_ACCESS_KEY_ID: optionalEnv('STORAGE_ACCESS_KEY_ID', ''),
  STORAGE_SECRET_ACCESS_KEY: optionalEnv('STORAGE_SECRET_ACCESS_KEY', ''),
  STORAGE_PUBLIC_DOMAIN: optionalEnv('STORAGE_PUBLIC_DOMAIN', ''),
  STORAGE_SIGNED_URL_EXPIRES: parseInt(optionalEnv('STORAGE_SIGNED_URL_EXPIRES', '900'), 10),

  // Auth
  JWT_SECRET: optionalEnv('JWT_SECRET', optionalEnv('SESSION_SECRET', 'dev-secret-change-in-production')),
  USE_MOCK_AUTH: optionalEnv('USE_MOCK_AUTH', 'false') === 'true',

  // Google
  GOOGLE_CLIENT_ID: optionalEnv('GOOGLE_CLIENT_ID', ''),
  GOOGLE_PLAY_PACKAGE_NAME: optionalEnv('GOOGLE_PLAY_PACKAGE_NAME', 'com.memora.familyphotos'),
  GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_BASE64: optionalEnv('GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_BASE64', ''),

  // Image Generation
  IMAGE_GENERATION_PROVIDER: optionalEnv('IMAGE_GENERATION_PROVIDER', 'mock'),

  // OpenAI
  OPENAI_API_KEY: optionalEnv('OPENAI_API_KEY', ''),

  // Sentry
  SENTRY_DSN: optionalEnv('SENTRY_DSN', ''),
  SENTRY_ENVIRONMENT: optionalEnv('SENTRY_ENVIRONMENT', optionalEnv('NODE_ENV', 'development')),

  // CORS
  ALLOWED_ORIGINS: optionalEnv('ALLOWED_ORIGINS', 'http://localhost:8080').split(','),
};
