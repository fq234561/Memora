import Database from 'better-sqlite3';
import path from 'path';

const DB_PATH = process.env.DATABASE_PATH || path.join(process.cwd(), 'memora.db');
const db: any = new Database(DB_PATH);

// Enable WAL mode for better concurrency
db.pragma('journal_mode = WAL');

export function getDb(): typeof db {
  return db;
}

export function initDatabase(): void {
  // Users table
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      email TEXT NOT NULL UNIQUE,
      name TEXT NOT NULL,
      avatar_url TEXT,
      created_at TEXT NOT NULL
    )
  `);

  // Projects table
  db.exec(`
    CREATE TABLE IF NOT EXISTS projects (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL,
      title TEXT NOT NULL,
      style TEXT NOT NULL,
      deceased_photo_url TEXT,
      living_photo_url TEXT,
      generated_photo_url TEXT,
      hd_photo_url TEXT,
      status TEXT NOT NULL DEFAULT 'DRAFT',
      consent_given INTEGER NOT NULL DEFAULT 0,
      regeneration_count INTEGER NOT NULL DEFAULT 0,
      regeneration_limit INTEGER NOT NULL DEFAULT 0,
      candidate_urls TEXT,
      selected_candidate_index INTEGER,
      purchased_product_id TEXT,
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL
    )
  `);

  // Purchases table
  db.exec(`
    CREATE TABLE IF NOT EXISTS purchases (
      id TEXT PRIMARY KEY,
      project_id TEXT NOT NULL,
      user_id TEXT NOT NULL,
      product_id TEXT NOT NULL,
      purchase_token TEXT NOT NULL UNIQUE,
      status TEXT NOT NULL DEFAULT 'PENDING',
      verified_at TEXT,
      created_at TEXT NOT NULL
    )
  `);

  // Generation history table
  db.exec(`
    CREATE TABLE IF NOT EXISTS generation_history (
      id TEXT PRIMARY KEY,
      project_id TEXT NOT NULL,
      type TEXT NOT NULL,
      timestamp TEXT NOT NULL,
      prompt TEXT NOT NULL,
      adjustment_prompt TEXT,
      candidate_urls TEXT NOT NULL,
      status TEXT NOT NULL
    )
  `);

  // Indexes
  db.exec(`CREATE INDEX IF NOT EXISTS idx_projects_user ON projects(user_id)`);
  db.exec(`CREATE INDEX IF NOT EXISTS idx_purchases_project ON purchases(project_id)`);
  db.exec(`CREATE INDEX IF NOT EXISTS idx_purchases_token ON purchases(purchase_token)`);
  db.exec(`CREATE INDEX IF NOT EXISTS idx_generation_project ON generation_history(project_id)`);
}
