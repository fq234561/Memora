import { getDb } from './db';
import { Project, Purchase, User, ProjectStatus, PurchaseStatus, PhotoStyle, GenerationHistoryEntry } from '../models/types';

class SQLiteStore {
  // User operations
  createUser(user: User): User {
    const stmt = getDb().prepare(`
      INSERT INTO users (id, email, name, avatar_url, created_at)
      VALUES (?, ?, ?, ?, ?)
      ON CONFLICT(id) DO UPDATE SET
        email = excluded.email,
        name = excluded.name,
        avatar_url = excluded.avatar_url
    `);
    stmt.run(user.id, user.email, user.name, user.avatarUrl || null, user.createdAt);
    return user;
  }

  getUser(id: string): User | undefined {
    const row = getDb().prepare('SELECT * FROM users WHERE id = ?').get(id) as any;
    if (!row) return undefined;
    return this.rowToUser(row);
  }

  getUserByEmail(email: string): User | undefined {
    const row = getDb().prepare('SELECT * FROM users WHERE email = ?').get(email) as any;
    if (!row) return undefined;
    return this.rowToUser(row);
  }

  // Project operations
  createProject(project: Project): Project {
    const initialized: Project = {
      ...project,
      regenerationCount: project.regenerationCount ?? 0,
      regenerationLimit: project.regenerationLimit ?? 0,
      generationHistory: project.generationHistory ?? [],
    };

    const stmt = getDb().prepare(`
      INSERT INTO projects (
        id, user_id, title, style, deceased_photo_url, living_photo_url,
        generated_photo_url, hd_photo_url, status, consent_given,
        regeneration_count, regeneration_limit, candidate_urls, selected_candidate_index,
        purchased_product_id, created_at, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);
    stmt.run(
      initialized.id,
      initialized.userId,
      initialized.title,
      initialized.style,
      initialized.deceasedPhotoUrl || null,
      initialized.livingPhotoUrl || null,
      initialized.generatedPhotoUrl || null,
      initialized.hdPhotoUrl || null,
      initialized.status,
      initialized.consentGiven ? 1 : 0,
      initialized.regenerationCount,
      initialized.regenerationLimit,
      initialized.candidateUrls ? JSON.stringify(initialized.candidateUrls) : null,
      initialized.selectedCandidateIndex ?? null,
      initialized.purchasedProductId || null,
      initialized.createdAt,
      initialized.updatedAt
    );

    // Save generation history
    if (initialized.generationHistory.length > 0) {
      this.saveGenerationHistory(initialized.id, initialized.generationHistory);
    }

    return initialized;
  }

  getProject(id: string): Project | undefined {
    const row = getDb().prepare('SELECT * FROM projects WHERE id = ?').get(id) as any;
    if (!row) return undefined;
    return this.rowToProject(row);
  }

  getProjectsByUser(userId: string): Project[] {
    const rows = getDb().prepare('SELECT * FROM projects WHERE user_id = ? ORDER BY created_at DESC').all(userId) as any[];
    return rows.map((r) => this.rowToProject(r));
  }

  updateProject(id: string, updates: Partial<Project>): Project | undefined {
    const existing = this.getProject(id);
    if (!existing) return undefined;

    const updated = { ...existing, ...updates, updatedAt: new Date().toISOString() };

    const fields: string[] = [];
    const values: any[] = [];

    if (updates.title !== undefined) { fields.push('title = ?'); values.push(updates.title); }
    if (updates.style !== undefined) { fields.push('style = ?'); values.push(updates.style); }
    if (updates.deceasedPhotoUrl !== undefined) { fields.push('deceased_photo_url = ?'); values.push(updates.deceasedPhotoUrl); }
    if (updates.livingPhotoUrl !== undefined) { fields.push('living_photo_url = ?'); values.push(updates.livingPhotoUrl); }
    if (updates.generatedPhotoUrl !== undefined) { fields.push('generated_photo_url = ?'); values.push(updates.generatedPhotoUrl); }
    if (updates.hdPhotoUrl !== undefined) { fields.push('hd_photo_url = ?'); values.push(updates.hdPhotoUrl); }
    if (updates.status !== undefined) { fields.push('status = ?'); values.push(updates.status); }
    if (updates.consentGiven !== undefined) { fields.push('consent_given = ?'); values.push(updates.consentGiven ? 1 : 0); }
    if (updates.regenerationCount !== undefined) { fields.push('regeneration_count = ?'); values.push(updates.regenerationCount); }
    if (updates.regenerationLimit !== undefined) { fields.push('regeneration_limit = ?'); values.push(updates.regenerationLimit); }
    if (updates.candidateUrls !== undefined) { fields.push('candidate_urls = ?'); values.push(updates.candidateUrls ? JSON.stringify(updates.candidateUrls) : null); }
    if (updates.selectedCandidateIndex !== undefined) { fields.push('selected_candidate_index = ?'); values.push(updates.selectedCandidateIndex); }
    if (updates.purchasedProductId !== undefined) { fields.push('purchased_product_id = ?'); values.push(updates.purchasedProductId); }

    fields.push('updated_at = ?');
    values.push(updated.updatedAt);
    values.push(id);

    if (fields.length > 0) {
      getDb().prepare(`UPDATE projects SET ${fields.join(', ')} WHERE id = ?`).run(...values);
    }

    // Save generation history if updated
    if (updates.generationHistory !== undefined) {
      getDb().prepare('DELETE FROM generation_history WHERE project_id = ?').run(id);
      this.saveGenerationHistory(id, updates.generationHistory);
    }

    return this.getProject(id);
  }

  deleteProject(id: string): boolean {
    const result = getDb().prepare('DELETE FROM projects WHERE id = ?').run(id);
    getDb().prepare('DELETE FROM generation_history WHERE project_id = ?').run(id);
    return result.changes > 0;
  }

  // Purchase operations
  createPurchase(purchase: Purchase): Purchase {
    const stmt = getDb().prepare(`
      INSERT INTO purchases (id, project_id, user_id, product_id, purchase_token, status, verified_at, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT(id) DO UPDATE SET
        status = excluded.status,
        verified_at = excluded.verified_at
    `);
    stmt.run(
      purchase.id,
      purchase.projectId,
      purchase.userId,
      purchase.productId,
      purchase.purchaseToken,
      purchase.status,
      purchase.verifiedAt || null,
      purchase.createdAt
    );
    return purchase;
  }

  getPurchase(id: string): Purchase | undefined {
    const row = getDb().prepare('SELECT * FROM purchases WHERE id = ?').get(id) as any;
    if (!row) return undefined;
    return this.rowToPurchase(row);
  }

  getPurchaseByToken(token: string): Purchase | undefined {
    const row = getDb().prepare('SELECT * FROM purchases WHERE purchase_token = ?').get(token) as any;
    if (!row) return undefined;
    return this.rowToPurchase(row);
  }

  // Stats
  getStats(): { users: number; projects: number; purchases: number } {
    const users = (getDb().prepare('SELECT COUNT(*) as c FROM users').get() as any).c;
    const projects = (getDb().prepare('SELECT COUNT(*) as c FROM projects').get() as any).c;
    const purchases = (getDb().prepare('SELECT COUNT(*) as c FROM purchases').get() as any).c;
    return { users, projects, purchases };
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

  private rowToProject(row: any): Project {
    const historyRows = getDb().prepare('SELECT * FROM generation_history WHERE project_id = ? ORDER BY timestamp').all(row.id) as any[];
    return {
      id: row.id,
      userId: row.user_id,
      title: row.title,
      style: row.style as PhotoStyle,
      deceasedPhotoUrl: row.deceased_photo_url || undefined,
      livingPhotoUrl: row.living_photo_url || undefined,
      generatedPhotoUrl: row.generated_photo_url || undefined,
      hdPhotoUrl: row.hd_photo_url || undefined,
      status: row.status as ProjectStatus,
      consentGiven: row.consent_given === 1,
      regenerationCount: row.regeneration_count,
      regenerationLimit: row.regeneration_limit,
      candidateUrls: row.candidate_urls ? JSON.parse(row.candidate_urls) : undefined,
      selectedCandidateIndex: row.selected_candidate_index ?? undefined,
      purchasedProductId: row.purchased_product_id || undefined,
      generationHistory: historyRows.map((h) => ({
        id: h.id,
        type: h.type,
        timestamp: h.timestamp,
        prompt: h.prompt,
        adjustmentPrompt: h.adjustment_prompt || undefined,
        candidateUrls: JSON.parse(h.candidate_urls),
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

  private saveGenerationHistory(projectId: string, history: GenerationHistoryEntry[]): void {
    const stmt = getDb().prepare(`
      INSERT INTO generation_history (id, project_id, type, timestamp, prompt, adjustment_prompt, candidate_urls, status)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `);
    for (const entry of history) {
      stmt.run(
        entry.id,
        projectId,
        entry.type,
        entry.timestamp,
        entry.prompt,
        entry.adjustmentPrompt || null,
        JSON.stringify(entry.candidateUrls),
        entry.status
      );
    }
  }
}

export const store = new SQLiteStore();
