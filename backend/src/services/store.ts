import { getPool } from './db';
import {
  Project,
  Purchase,
  User,
  ProjectStatus,
  PurchaseStatus,
  PhotoStyle,
  GenerationHistoryEntry,
} from '../models/types';

class PostgresStore {
  // User operations
  async createUser(user: User): Promise<User> {
    const db = getPool();
    await db.query(
      `INSERT INTO users (id, email, name, avatar_url, created_at)
       VALUES ($1, $2, $3, $4, $5)
       ON CONFLICT(id) DO UPDATE SET
         email = excluded.email,
         name = excluded.name,
         avatar_url = excluded.avatar_url`,
      [user.id, user.email, user.name, user.avatarUrl || null, user.createdAt]
    );
    return user;
  }

  async getUser(id: string): Promise<User | undefined> {
    const db = getPool();
    const result = await db.query('SELECT * FROM users WHERE id = $1', [id]);
    const row = result.rows[0];
    if (!row) return undefined;
    return this.rowToUser(row);
  }

  async getUserByEmail(email: string): Promise<User | undefined> {
    const db = getPool();
    const result = await db.query('SELECT * FROM users WHERE email = $1', [email]);
    const row = result.rows[0];
    if (!row) return undefined;
    return this.rowToUser(row);
  }

  // Project operations
  async createProject(project: Project): Promise<Project> {
    const db = getPool();
    const initialized: Project = {
      ...project,
      regenerationCount: project.regenerationCount ?? 0,
      regenerationLimit: project.regenerationLimit ?? 0,
      generationHistory: project.generationHistory ?? [],
    };

    await db.query(
      `INSERT INTO projects (
        id, user_id, title, style, deceased_photo_key, living_photo_key,
        generated_photo_key, hd_photo_key, status, consent_given,
        regeneration_count, regeneration_limit, candidate_keys, selected_candidate_index,
        purchased_product_id, created_at, updated_at
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17)`,
      [
        initialized.id,
        initialized.userId,
        initialized.title,
        initialized.style,
        initialized.deceasedPhotoUrl || null,
        initialized.livingPhotoUrl || null,
        initialized.generatedPhotoUrl || null,
        initialized.hdPhotoUrl || null,
        initialized.status,
        initialized.consentGiven,
        initialized.regenerationCount,
        initialized.regenerationLimit,
        initialized.candidateUrls ? JSON.stringify(initialized.candidateUrls) : null,
        initialized.selectedCandidateIndex ?? null,
        initialized.purchasedProductId || null,
        initialized.createdAt,
        initialized.updatedAt,
      ]
    );

    if (initialized.generationHistory.length > 0) {
      await this.saveGenerationHistory(initialized.id, initialized.generationHistory);
    }

    return initialized;
  }

  async getProject(id: string): Promise<Project | undefined> {
    const db = getPool();
    const result = await db.query('SELECT * FROM projects WHERE id = $1', [id]);
    const row = result.rows[0];
    if (!row) return undefined;
    return this.rowToProject(row);
  }

  async getProjectsByUser(userId: string): Promise<Project[]> {
    const db = getPool();
    const result = await db.query(
      'SELECT * FROM projects WHERE user_id = $1 ORDER BY created_at DESC',
      [userId]
    );
    return Promise.all(result.rows.map((r) => this.rowToProject(r)));
  }

  async updateProject(id: string, updates: Partial<Project>): Promise<Project | undefined> {
    const db = getPool();
    const existing = await this.getProject(id);
    if (!existing) return undefined;

    const updated = { ...existing, ...updates, updatedAt: new Date().toISOString() };

    const fields: string[] = [];
    const values: any[] = [];
    let paramIndex = 1;

    if (updates.title !== undefined) { fields.push(`title = $${paramIndex++}`); values.push(updates.title); }
    if (updates.style !== undefined) { fields.push(`style = $${paramIndex++}`); values.push(updates.style); }
    if (updates.deceasedPhotoUrl !== undefined) { fields.push(`deceased_photo_key = $${paramIndex++}`); values.push(updates.deceasedPhotoUrl); }
    if (updates.livingPhotoUrl !== undefined) { fields.push(`living_photo_key = $${paramIndex++}`); values.push(updates.livingPhotoUrl); }
    if (updates.generatedPhotoUrl !== undefined) { fields.push(`generated_photo_key = $${paramIndex++}`); values.push(updates.generatedPhotoUrl); }
    if (updates.hdPhotoUrl !== undefined) { fields.push(`hd_photo_key = $${paramIndex++}`); values.push(updates.hdPhotoUrl); }
    if (updates.status !== undefined) { fields.push(`status = $${paramIndex++}`); values.push(updates.status); }
    if (updates.consentGiven !== undefined) { fields.push(`consent_given = $${paramIndex++}`); values.push(updates.consentGiven); }
    if (updates.regenerationCount !== undefined) { fields.push(`regeneration_count = $${paramIndex++}`); values.push(updates.regenerationCount); }
    if (updates.regenerationLimit !== undefined) { fields.push(`regeneration_limit = $${paramIndex++}`); values.push(updates.regenerationLimit); }
    if (updates.candidateUrls !== undefined) { fields.push(`candidate_keys = $${paramIndex++}`); values.push(updates.candidateUrls ? JSON.stringify(updates.candidateUrls) : null); }
    if (updates.selectedCandidateIndex !== undefined) { fields.push(`selected_candidate_index = $${paramIndex++}`); values.push(updates.selectedCandidateIndex); }
    if (updates.purchasedProductId !== undefined) { fields.push(`purchased_product_id = $${paramIndex++}`); values.push(updates.purchasedProductId); }

    fields.push(`updated_at = $${paramIndex++}`);
    values.push(updated.updatedAt);
    values.push(id);

    if (fields.length > 0) {
      await db.query(
        `UPDATE projects SET ${fields.join(', ')} WHERE id = $${paramIndex}`,
        values
      );
    }

    // Save generation history if updated (wrapped in transaction)
    if (updates.generationHistory !== undefined) {
      const client = await db.connect();
      try {
        await client.query('BEGIN');
        await client.query('DELETE FROM generation_history WHERE project_id = $1', [id]);
        await this.saveGenerationHistoryWithClient(client, id, updates.generationHistory);
        await client.query('COMMIT');
      } catch (err) {
        await client.query('ROLLBACK');
        throw err;
      } finally {
        client.release();
      }
    }

    return this.getProject(id);
  }

  async deleteProject(id: string): Promise<boolean> {
    const db = getPool();
    const result = await db.query('DELETE FROM projects WHERE id = $1', [id]);
    return (result.rowCount ?? 0) > 0;
  }

  // Purchase operations
  async createPurchase(purchase: Purchase): Promise<Purchase> {
    const db = getPool();
    await db.query(
      `INSERT INTO purchases (id, project_id, user_id, product_id, purchase_token, status, verified_at, created_at)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
       ON CONFLICT(id) DO UPDATE SET
         status = excluded.status,
         verified_at = excluded.verified_at`,
      [
        purchase.id,
        purchase.projectId,
        purchase.userId,
        purchase.productId,
        purchase.purchaseToken,
        purchase.status,
        purchase.verifiedAt || null,
        purchase.createdAt,
      ]
    );
    return purchase;
  }

  async getPurchase(id: string): Promise<Purchase | undefined> {
    const db = getPool();
    const result = await db.query('SELECT * FROM purchases WHERE id = $1', [id]);
    const row = result.rows[0];
    if (!row) return undefined;
    return this.rowToPurchase(row);
  }

  async getPurchaseByToken(token: string): Promise<Purchase | undefined> {
    const db = getPool();
    const result = await db.query('SELECT * FROM purchases WHERE purchase_token = $1', [token]);
    const row = result.rows[0];
    if (!row) return undefined;
    return this.rowToPurchase(row);
  }

  // Stats
  async getStats(): Promise<{ users: number; projects: number; purchases: number }> {
    const db = getPool();
    const usersResult = await db.query('SELECT COUNT(*) as c FROM users');
    const projectsResult = await db.query('SELECT COUNT(*) as c FROM projects');
    const purchasesResult = await db.query('SELECT COUNT(*) as c FROM purchases');
    return {
      users: parseInt(usersResult.rows[0].c, 10),
      projects: parseInt(projectsResult.rows[0].c, 10),
      purchases: parseInt(purchasesResult.rows[0].c, 10),
    };
  }

  // Contact messages
  async createContactMessage(data: {
    id: string;
    userId?: string;
    type: string;
    email?: string;
    message: string;
    metadata?: Record<string, unknown>;
  }): Promise<void> {
    const db = getPool();
    await db.query(
      `INSERT INTO contact_messages (id, user_id, type, email, message, metadata, created_at)
       VALUES ($1, $2, $3, $4, $5, $6, NOW())`,
      [data.id, data.userId || null, data.type, data.email || null, data.message, JSON.stringify(data.metadata || {})]
    );
  }

  // Helpers
  private rowToUser(row: any): User {
    return {
      id: row.id,
      email: row.email,
      name: row.name,
      avatarUrl: row.avatar_url || undefined,
      createdAt: row.created_at,
    };
  }

  private async rowToProject(row: any): Promise<Project> {
    const db = getPool();
    const historyResult = await db.query(
      'SELECT * FROM generation_history WHERE project_id = $1 ORDER BY timestamp',
      [row.id]
    );
    return {
      id: row.id,
      userId: row.user_id,
      title: row.title,
      style: row.style as PhotoStyle,
      deceasedPhotoUrl: row.deceased_photo_key || undefined,
      livingPhotoUrl: row.living_photo_key || undefined,
      generatedPhotoUrl: row.generated_photo_key || undefined,
      hdPhotoUrl: row.hd_photo_key || undefined,
      status: row.status as ProjectStatus,
      consentGiven: row.consent_given === true,
      regenerationCount: row.regeneration_count,
      regenerationLimit: row.regeneration_limit,
      candidateUrls: row.candidate_keys ? (Array.isArray(row.candidate_keys) ? row.candidate_keys : JSON.parse(row.candidate_keys)) : undefined,
      selectedCandidateIndex: row.selected_candidate_index ?? undefined,
      purchasedProductId: row.purchased_product_id || undefined,
      generationHistory: historyResult.rows.map((h: any) => ({
        id: h.id,
        type: h.type,
        timestamp: h.timestamp,
        prompt: h.prompt,
        adjustmentPrompt: h.adjustment_prompt || undefined,
        candidateUrls: Array.isArray(h.candidate_keys) ? h.candidate_keys : JSON.parse(h.candidate_keys),
        status: h.status,
      })),
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }

  private rowToPurchase(row: any): Purchase {
    return {
      id: row.id,
      projectId: row.project_id,
      userId: row.user_id,
      productId: row.product_id,
      purchaseToken: row.purchase_token,
      status: row.status as PurchaseStatus,
      verifiedAt: row.verified_at || undefined,
      createdAt: row.created_at,
    };
  }

  private async saveGenerationHistory(projectId: string, history: GenerationHistoryEntry[]): Promise<void> {
    const db = getPool();
    for (const entry of history) {
      await db.query(
        `INSERT INTO generation_history (id, project_id, type, timestamp, prompt, adjustment_prompt, candidate_keys, status)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
        [
          entry.id,
          projectId,
          entry.type,
          entry.timestamp,
          entry.prompt,
          entry.adjustmentPrompt || null,
          JSON.stringify(entry.candidateUrls),
          entry.status,
        ]
      );
    }
  }

  private async saveGenerationHistoryWithClient(
    client: any,
    projectId: string,
    history: GenerationHistoryEntry[]
  ): Promise<void> {
    for (const entry of history) {
      await client.query(
        `INSERT INTO generation_history (id, project_id, type, timestamp, prompt, adjustment_prompt, candidate_keys, status)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
        [
          entry.id,
          projectId,
          entry.type,
          entry.timestamp,
          entry.prompt,
          entry.adjustmentPrompt || null,
          JSON.stringify(entry.candidateUrls),
          entry.status,
        ]
      );
    }
  }
}

export const store = new PostgresStore();
