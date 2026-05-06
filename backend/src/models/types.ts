export interface User {
  id: string;
  email: string;
  name: string;
  avatarUrl?: string;
  createdAt: string;
}

export interface GenerationHistoryEntry {
  id: string;
  type: 'initial' | 'regenerate';
  timestamp: string;
  prompt: string;
  adjustmentPrompt?: string;
  candidateUrls: string[];
  status: 'success' | 'failed';
}

export interface Project {
  id: string;
  userId: string;
  title: string;
  style: PhotoStyle;
  /** @deprecated Legacy field name. Semantically now represents the person reference photo. Use personPhotoUrl. */
  deceasedPhotoUrl?: string;
  /** @deprecated Legacy field name. Semantically now represents the event/base reference photo. Use basePhotoUrl. */
  livingPhotoUrl?: string;
  basePhotoUrl?: string;
  personPhotoUrl?: string;
  generatedPhotoUrl?: string;
  hdPhotoUrl?: string;
  status: ProjectStatus;
  consentGiven: boolean;
  regenerationCount: number;
  regenerationLimit: number;
  candidateUrls?: string[];
  selectedCandidateIndex?: number;
  purchasedProductId?: string;
  generationHistory: GenerationHistoryEntry[];
  eventDate?: string;
  activityType?: ActivityType;
  personTypes?: PersonType[];
  detectedTags?: string[];
  albumId?: string | null;
  createdAt: string;
  updatedAt: string;
}

export enum PhotoStyle {
  NATURAL_FAMILY = 'NATURAL_FAMILY',
  TRAVEL_MEMORY = 'TRAVEL_MEMORY',
  PARTY_GATHERING = 'PARTY_GATHERING',
  HOLIDAY_CELEBRATION = 'HOLIDAY_CELEBRATION',
  MILESTONE_EVENT = 'MILESTONE_EVENT',
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
  eventDate?: string;
  activityType?: ActivityType;
  personTypes?: PersonType[];
}

export interface GenerateRequest {
  projectId: string;
}

export interface StatusResponse {
  status: ProjectStatus;
  progress?: number;
  resultUrl?: string;
  candidateUrls?: string[];
  regenerationRemaining?: number;
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

export enum ActivityType {
  TRAVEL = 'TRAVEL',
  PARTY = 'PARTY',
  HOLIDAY = 'HOLIDAY',
  BIRTHDAY = 'BIRTHDAY',
  WEDDING = 'WEDDING',
  GRADUATION = 'GRADUATION',
  REUNION = 'REUNION',
  DAILY = 'DAILY',
  OTHER = 'OTHER',
}

export enum PersonType {
  PARENT = 'PARENT',
  GRANDPARENT = 'GRANDPARENT',
  CHILD = 'CHILD',
  SIBLING = 'SIBLING',
  PARTNER = 'PARTNER',
  FRIEND = 'FRIEND',
  RELATIVE = 'RELATIVE',
  PET = 'PET',
  OTHER = 'OTHER',
}

export interface Album {
  id: string;
  userId: string;
  title: string;
  projectIds: string[];
  status: AlbumStatus;
  pdfUrl?: string;
  mp4Url?: string;
  createdAt: string;
  updatedAt: string;
}

export enum AlbumStatus {
  DRAFT = 'DRAFT',
  RENDERING = 'RENDERING',
  READY = 'READY',
  FAILED = 'FAILED',
}

export interface CreateAlbumRequest {
  title: string;
  projectIds: string[];
}
