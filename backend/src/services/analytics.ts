import { getPool } from './db';
import { v4 as uuidv4 } from 'uuid';
import { FunnelEventName } from '../models/types';

export async function trackEvent(params: {
  eventName: FunnelEventName;
  userId?: string;
  anonymousId?: string;
  projectId?: string;
  metadata?: Record<string, unknown>;
}): Promise<void> {
  try {
    const db = getPool();
    await db.query(
      `INSERT INTO events (id, user_id, anonymous_id, project_id, event_name, metadata, created_at)
       VALUES ($1, $2, $3, $4, $5, $6, NOW())`,
      [
        uuidv4(),
        params.userId || null,
        params.anonymousId || null,
        params.projectId || null,
        params.eventName,
        JSON.stringify(params.metadata || {}),
      ]
    );
  } catch (err) {
    console.error('[analytics] Failed to track event:', err);
  }
}

export async function getEventsByProject(projectId: string): Promise<{ eventName: string; createdAt: string }[]> {
  const db = getPool();
  const result = await db.query(
    `SELECT event_name, created_at FROM events WHERE project_id = $1 ORDER BY created_at ASC`,
    [projectId]
  );
  return result.rows.map((r) => ({ eventName: r.event_name, createdAt: r.created_at }));
}

export async function getFunnelStats(
  userId?: string,
  startDate?: string,
  endDate?: string
): Promise<Record<string, number>> {
  const db = getPool();
  const conditions: string[] = [];
  const values: any[] = [];
  let paramIndex = 1;

  if (userId) {
    conditions.push(`user_id = $${paramIndex++}`);
    values.push(userId);
  }
  if (startDate) {
    conditions.push(`created_at >= $${paramIndex++}`);
    values.push(startDate);
  }
  if (endDate) {
    conditions.push(`created_at <= $${paramIndex++}`);
    values.push(endDate);
  }

  const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';

  const result = await db.query(
    `SELECT event_name, COUNT(DISTINCT id) as count FROM events ${whereClause} GROUP BY event_name`,
    values
  );

  const stats: Record<string, number> = {};
  for (const row of result.rows) {
    stats[row.event_name] = parseInt(row.count, 10);
  }
  return stats;
}
