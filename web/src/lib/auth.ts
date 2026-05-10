'use client';

import { loginWithGoogle } from './api';

declare global {
  interface Window {
    google?: any;
  }
}

export function initGoogleSignIn(onCredential: (credential: string) => void) {
  if (typeof window === 'undefined') return;
  const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
  if (!clientId) {
    console.error('NEXT_PUBLIC_GOOGLE_CLIENT_ID is not set');
    return;
  }

  const runInit = () => {
    if (!window.google) return;
    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: (response: any) => {
        if (response.credential) {
          onCredential(response.credential);
        }
      },
      auto_select: false,
      locale: 'en',
    });
  };

  if (window.google) {
    runInit();
  } else {
    const check = setInterval(() => {
      if (window.google) {
        clearInterval(check);
        runInit();
      }
    }, 200);
    setTimeout(() => clearInterval(check), 10000);
  }
}

export function renderGoogleButton(buttonId: string) {
  if (typeof window === 'undefined' || !window.google) return;
  const el = document.getElementById(buttonId);
  if (!el) return;
  window.google.accounts.id.renderButton(el, {
    theme: 'outline',
    size: 'large',
    width: el.clientWidth > 0 ? el.clientWidth : 320,
    text: 'signin_with',
  });
}

export async function handleGoogleLogin(credential: string) {
  const data = await loginWithGoogle(credential);
  if (data.accessToken) {
    localStorage.setItem('memora_token', data.accessToken);
    localStorage.setItem('memora_user', JSON.stringify(data.user));
  }
  return data;
}

export function logout() {
  localStorage.removeItem('memora_token');
  localStorage.removeItem('memora_user');
  if (typeof window !== 'undefined') {
    window.location.href = '/';
  }
}

export function getStoredUser(): any | null {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('memora_user');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function isLoggedIn(): boolean {
  if (typeof window === 'undefined') return false;
  return !!localStorage.getItem('memora_token');
}
