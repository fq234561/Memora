export interface User {
  id: string;
  email: string;
  name: string;
  avatarUrl?: string;
  createdAt: string;
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

export interface Project {
  id: string;
  userId: string;
  title: string;
  style: PhotoStyle;
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
  eventDate?: string;
  activityType?: ActivityType;
  personTypes?: PersonType[];
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}
