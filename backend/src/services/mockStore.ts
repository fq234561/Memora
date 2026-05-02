import { Project, Purchase, User, ProjectStatus, PurchaseStatus, PhotoStyle } from '../models/types';

// In-memory mock store - will be replaced with real database in production
class MockStore {
  private users = new Map<string, User>();
  private projects = new Map<string, Project>();
  private purchases = new Map<string, Purchase>();

  // User operations
  createUser(user: User): User {
    this.users.set(user.id, user);
    return user;
  }

  getUser(id: string): User | undefined {
    return this.users.get(id);
  }

  // Project operations
  createProject(project: Project): Project {
    const initialized: Project = {
      ...project,
      regenerationCount: project.regenerationCount ?? 0,
      regenerationLimit: project.regenerationLimit ?? 0,
      generationHistory: project.generationHistory ?? [],
    };
    this.projects.set(project.id, initialized);
    return initialized;
  }

  getProject(id: string): Project | undefined {
    return this.projects.get(id);
  }

  getProjectsByUser(userId: string): Project[] {
    return Array.from(this.projects.values())
      .filter((p) => p.userId === userId)
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  updateProject(id: string, updates: Partial<Project>): Project | undefined {
    const project = this.projects.get(id);
    if (!project) return undefined;
    const updated = { ...project, ...updates, updatedAt: new Date().toISOString() };
    this.projects.set(id, updated);
    return updated;
  }

  deleteProject(id: string): boolean {
    return this.projects.delete(id);
  }

  // Purchase operations
  createPurchase(purchase: Purchase): Purchase {
    this.purchases.set(purchase.id, purchase);
    return purchase;
  }

  getPurchase(id: string): Purchase | undefined {
    return this.purchases.get(id);
  }

  // Stats
  getStats(): { users: number; projects: number; purchases: number } {
    return {
      users: this.users.size,
      projects: this.projects.size,
      purchases: this.purchases.size,
    };
  }
}

export const store = new MockStore();
