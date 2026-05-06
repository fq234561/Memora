import { Pool } from 'pg';
import { env } from '../utils/env';

let pool: Pool | null = null;

export function getPool(): Pool {
  if (!pool) {
    if (!env.DATABASE_URL) {
      throw new Error('DATABASE_URL is not configured');
    }
    pool = new Pool({
      connectionString: env.DATABASE_URL,
      ssl: env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : undefined,
    });
  }
  return pool;
}

export async function initDatabase(): Promise<void> {
  const db = getPool();

  // Users table
  await db.query(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      email TEXT NOT NULL UNIQUE,
      name TEXT NOT NULL,
      avatar_url TEXT,
      created_at TIMESTAMPTZ NOT NULL
    )
  `);

  // Projects table
  await db.query(`
    CREATE TABLE IF NOT EXISTS projects (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      title TEXT NOT NULL,
      style TEXT NOT NULL,
      deceased_photo_key TEXT,
      living_photo_key TEXT,
      generated_photo_key TEXT,
      hd_photo_key TEXT,
      status TEXT NOT NULL DEFAULT 'DRAFT',
      consent_given BOOLEAN NOT NULL DEFAULT false,
      regeneration_count INTEGER NOT NULL DEFAULT 0,
      regeneration_limit INTEGER NOT NULL DEFAULT 0,
      candidate_keys JSONB,
      selected_candidate_index INTEGER,
      purchased_product_id TEXT,
      event_date TEXT,
      activity_type TEXT,
      person_types JSONB,
      detected_tags JSONB,
      album_id TEXT,
      created_at TIMESTAMPTZ NOT NULL,
      updated_at TIMESTAMPTZ NOT NULL
    )
  `);

  // Migration: add new columns to existing projects table safely
  await db.query(`ALTER TABLE projects ADD COLUMN IF NOT EXISTS event_date TEXT`);
  await db.query(`ALTER TABLE projects ADD COLUMN IF NOT EXISTS activity_type TEXT`);
  await db.query(`ALTER TABLE projects ADD COLUMN IF NOT EXISTS person_types JSONB`);
  await db.query(`ALTER TABLE projects ADD COLUMN IF NOT EXISTS detected_tags JSONB`);
  await db.query(`ALTER TABLE projects ADD COLUMN IF NOT EXISTS album_id TEXT`);

  // Purchases table
  await db.query(`
    CREATE TABLE IF NOT EXISTS purchases (
      id TEXT PRIMARY KEY,
      project_id TEXT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      product_id TEXT NOT NULL,
      purchase_token TEXT NOT NULL UNIQUE,
      status TEXT NOT NULL DEFAULT 'PENDING',
      verified_at TIMESTAMPTZ,
      created_at TIMESTAMPTZ NOT NULL
    )
  `);

  // Generation history table
  await db.query(`
    CREATE TABLE IF NOT EXISTS generation_history (
      id TEXT PRIMARY KEY,
      project_id TEXT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
      type TEXT NOT NULL,
      timestamp TIMESTAMPTZ NOT NULL,
      prompt TEXT NOT NULL,
      adjustment_prompt TEXT,
      candidate_keys JSONB NOT NULL,
      status TEXT NOT NULL
    )
  `);

  // Albums table
  await db.query(`
    CREATE TABLE IF NOT EXISTS albums (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      title TEXT NOT NULL,
      project_ids JSONB NOT NULL,
      status TEXT NOT NULL DEFAULT 'DRAFT',
      pdf_key TEXT,
      mp4_key TEXT,
      created_at TIMESTAMPTZ NOT NULL,
      updated_at TIMESTAMPTZ NOT NULL
    )
  `);

  // Contact messages table
  await db.query(`
    CREATE TABLE IF NOT EXISTS contact_messages (
      id TEXT PRIMARY KEY,
      user_id TEXT REFERENCES users(id) ON DELETE SET NULL,
      type TEXT NOT NULL,
      email TEXT,
      message TEXT NOT NULL,
      metadata JSONB DEFAULT '{}',
      created_at TIMESTAMPTZ DEFAULT NOW()
    )
  `);

  // Indexes
  await db.query(`CREATE INDEX IF NOT EXISTS idx_projects_user ON projects(user_id)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_projects_album ON projects(album_id)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_projects_activity ON projects(activity_type)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_purchases_project ON purchases(project_id)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_purchases_token ON purchases(purchase_token)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_generation_project ON generation_history(project_id)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_albums_user ON albums(user_id)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_contact_user ON contact_messages(user_id)`);
  await db.query(`CREATE INDEX IF NOT EXISTS idx_contact_type ON contact_messages(type)`);
}

export async function closeDatabase(): Promise<void> {
  if (pool) {
    await pool.end();
    pool = null;
  }
}
