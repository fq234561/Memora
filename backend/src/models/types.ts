export interface User {
  id: string;
  email: string;
  name: string;
  avatarUrl?: string;
  createdAt: string;
}

export interface Project {
  id: string;
  userId: string;
  title: string;
  style: PhotoStyle;
  deceasedPhotoUrl?: string;
  livingPhotoUrl?: string;
  generatedPhotoUrl?: string;
  hdPhotoUrl?: string;
  status: ProjectStatus;
  consentGiven: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum PhotoStyle {
  NATURAL_FAMILY = 'NATURAL_FAMILY',
  VINTAGE_RESTORE = 'VINTAGE_RESTORE',
  BIRTHDAY = 'BIRTHDAY',
  GRADUATION_WEDDING_HOLIDAY = 'GRADUATION_WEDDING_HOLIDAY',
}

export enum ProjectStatus {
  DRAFT = 'DRAFT',
  UPLOADED = 'UPLOADED',
  GENERATING = 'GENERATING',
  PREVIEW_READY = 'PREVIEW_READY',
  PURCHASED = 'PURCHASED',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export interface Purchase {
  id: string;
  projectId: string;
  userId: string;
  productId: string;
  purchaseToken: string;
  status: PurchaseStatus;
  verifiedAt?: string;
  createdAt: string;
}

export enum PurchaseStatus {
  PENDING = 'PENDING',
  VERIFIED = 'VERIFIED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
}

export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface AuthRequest {
  idToken: string;
}

export interface CreateProjectRequest {
  title: string;
  style: PhotoStyle;
}

export interface UploadUrlResponse {
  uploadUrl: string;
  fileKey: string;
}

export interface GenerateRequest {
  projectId: string;
}

export interface StatusResponse {
  status: ProjectStatus;
  progress?: number;
  resultUrl?: string;
}

export interface PurchaseRequest {
  projectId: string;
  productId: string;
  purchaseToken: string;
}

export interface ContactRequest {
  type: 'feedback' | 'report' | 'deletion';
  email: string;
  message: string;
  projectId?: string;
}
