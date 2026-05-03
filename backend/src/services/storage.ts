import { S3Client, PutObjectCommand, DeleteObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { env } from '../utils/env';

let s3Client: S3Client | null = null;

function getClient(): S3Client {
  if (!s3Client) {
    s3Client = new S3Client({
      endpoint: env.STORAGE_ENDPOINT,
      region: 'auto',
      credentials: {
        accessKeyId: env.STORAGE_ACCESS_KEY_ID,
        secretAccessKey: env.STORAGE_SECRET_ACCESS_KEY,
      },
      forcePathStyle: true,
    });
  }
  return s3Client;
}

export async function uploadFile(key: string, buffer: Buffer, contentType: string): Promise<void> {
  const client = getClient();
  const command = new PutObjectCommand({
    Bucket: env.STORAGE_BUCKET,
    Key: key,
    Body: buffer,
    ContentType: contentType,
  });
  await client.send(command);
}

export async function deleteFile(key: string): Promise<void> {
  const client = getClient();
  const command = new DeleteObjectCommand({
    Bucket: env.STORAGE_BUCKET,
    Key: key,
  });
  await client.send(command);
}

export async function getSignedDownloadUrl(key: string): Promise<string> {
  if (!key) return '';
  // External URLs (e.g., picsum mock images) pass through as-is
  if (key.startsWith('http')) return key;

  const client = getClient();
  const command = new GetObjectCommand({
    Bucket: env.STORAGE_BUCKET,
    Key: key,
  });

  // Use custom domain if configured, otherwise use R2 endpoint
  const domain = env.STORAGE_PUBLIC_DOMAIN || env.STORAGE_ENDPOINT;

  const signedUrl = await getSignedUrl(client, command, {
    expiresIn: env.STORAGE_SIGNED_URL_EXPIRES,
  });

  // If using custom domain, replace the endpoint in the signed URL
  if (env.STORAGE_PUBLIC_DOMAIN && env.STORAGE_ENDPOINT) {
    return signedUrl.replace(env.STORAGE_ENDPOINT, env.STORAGE_PUBLIC_DOMAIN);
  }

  return signedUrl;
}

export function getPublicUrl(key: string): string {
  if (!key) return '';
  if (key.startsWith('http')) return key;

  const domain = env.STORAGE_PUBLIC_DOMAIN || env.STORAGE_ENDPOINT;
  return `${domain}/${env.STORAGE_BUCKET}/${key}`;
}

export async function getSignedUploadUrl(key: string, contentType: string): Promise<string> {
  const client = getClient();
  const command = new PutObjectCommand({
    Bucket: env.STORAGE_BUCKET,
    Key: key,
    ContentType: contentType,
  });

  const signedUrl = await getSignedUrl(client, command, {
    expiresIn: env.STORAGE_SIGNED_URL_EXPIRES,
  });

  if (env.STORAGE_PUBLIC_DOMAIN && env.STORAGE_ENDPOINT) {
    return signedUrl.replace(env.STORAGE_ENDPOINT, env.STORAGE_PUBLIC_DOMAIN);
  }

  return signedUrl;
}
