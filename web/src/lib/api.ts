import axios from 'axios';
import { ApiResponse } from './types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:3000';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = typeof window !== 'undefined' ? localStorage.getItem('memora_token') : null;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      localStorage.removeItem('memora_token');
      localStorage.removeItem('memora_user');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default api;

export async function loginWithGoogle(idToken: string) {
  const res = await api.post<ApiResponse<{ accessToken: string; user: any }>>('/auth/google', { idToken });
  return res.data.data!;
}

export async function getProjects() {
  const res = await api.get<ApiResponse<any[]>>('/projects');
  return res.data.data!;
}

export async function getProject(projectId: string) {
  const res = await api.get<ApiResponse<any>>(`/projects/${projectId}`);
  return res.data.data!;
}

export async function createProject(data: {
  title: string;
  style: string;
  eventDate?: string;
  activityType?: string;
  personTypes?: string[];
}) {
  const res = await api.post<ApiResponse<any>>('/projects', data);
  return res.data.data!;
}

export async function uploadPhoto(projectId: string, file: File, type: 'base' | 'person') {
  const formData = new FormData();
  formData.append('photo', file);
  formData.append('type', type);
  const res = await api.post<ApiResponse<{ project: any }>>(`/projects/${projectId}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data.data!;
}

export async function giveConsent(projectId: string) {
  const res = await api.post<ApiResponse<any>>(`/projects/${projectId}/consent`);
  return res.data.data!;
}

export async function generatePreview(projectId: string, customPrompt?: string, isRegeneration?: boolean, adjustmentPrompt?: string) {
  const res = await api.post<ApiResponse<any>>(`/projects/${projectId}/generate`, {
    customPrompt,
    isRegeneration,
    adjustmentPrompt,
  });
  return res.data.data!;
}

export async function getProjectStatus(projectId: string) {
  const res = await api.get<ApiResponse<{
    status: string;
    progress?: number;
    resultUrl?: string;
    candidateUrls?: string[];
    regenerationRemaining?: number;
  }>>(`/projects/${projectId}/status`);
  return res.data.data!;
}

export async function selectCandidate(projectId: string, index: number) {
  const res = await api.post<ApiResponse<any>>(`/projects/${projectId}/select-candidate`, { index });
  return res.data.data!;
}

export async function deleteProject(projectId: string) {
  const res = await api.delete<ApiResponse<any>>(`/projects/${projectId}`);
  return res.data;
}

export async function createStripeCheckout(projectId: string, productId: string) {
  const res = await api.post<ApiResponse<{ url: string; sessionId: string }>>('/stripe/create-checkout-session', {
    projectId,
    productId,
  });
  return res.data.data!;
}

