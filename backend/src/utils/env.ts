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
  DATABASE_URL: optionalEnv('DATABASE_URL', ''),
  STORAGE_ENDPOINT: optionalEnv('STORAGE_ENDPOINT', ''),
  STORAGE_BUCKET: optionalEnv('STORAGE_BUCKET', 'memorial-assets'),
  STORAGE_ACCESS_KEY_ID: optionalEnv('STORAGE_ACCESS_KEY_ID', ''),
  STORAGE_SECRET_ACCESS_KEY: optionalEnv('STORAGE_SECRET_ACCESS_KEY', ''),
  OPENAI_API_KEY: optionalEnv('OPENAI_API_KEY', ''),
  GOOGLE_CLIENT_ID: optionalEnv('GOOGLE_CLIENT_ID', ''),
  GOOGLE_PLAY_PACKAGE_NAME: optionalEnv('GOOGLE_PLAY_PACKAGE_NAME', 'com.memorial.app'),
  GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_BASE64: optionalEnv('GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_BASE64', ''),
  SESSION_SECRET: requireEnv('SESSION_SECRET'),
  ALLOWED_ORIGINS: optionalEnv('ALLOWED_ORIGINS', 'http://localhost:8080').split(','),
};
