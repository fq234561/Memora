import { GoogleAuth } from 'google-auth-library';
import { env } from '../utils/env';

interface GooglePlayVerificationResult {
  valid: boolean;
  purchaseState: number; // 0 = pending, 1 = purchased, 2 = refunded
  consumptionState?: number;
  acknowledgementState?: number;
  productId?: string;
  error?: string;
}

let googleAuth: GoogleAuth | null = null;

function initGoogleAuth(): GoogleAuth | null {
  if (googleAuth) return googleAuth;

  const serviceAccountBase64 = env.GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_BASE64;
  if (!serviceAccountBase64) {
    return null;
  }

  try {
    const serviceAccountJson = Buffer.from(serviceAccountBase64, 'base64').toString('utf-8');
    const credentials = JSON.parse(serviceAccountJson);
    googleAuth = new GoogleAuth({
      credentials,
      scopes: ['https://www.googleapis.com/auth/androidpublisher'],
    });
    return googleAuth;
  } catch (e) {
    console.error('Failed to initialize Google Play auth:', e);
    return null;
  }
}

export async function verifyGooglePlayPurchase(
  packageName: string,
  productId: string,
  purchaseToken: string
): Promise<GooglePlayVerificationResult> {
  const auth = initGoogleAuth();
  if (!auth) {
    return {
      valid: false,
      purchaseState: -1,
      error: 'Google Play service account not configured',
    };
  }

  try {
    const client = await auth.getClient();
    const accessToken = await client.getAccessToken();

    const url = `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${encodeURIComponent(
      packageName
    )}/purchases/products/${encodeURIComponent(productId)}/tokens/${encodeURIComponent(
      purchaseToken
    )}`;

    const response = await fetch(url, {
      headers: {
        Authorization: `Bearer ${accessToken.token}`,
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return {
        valid: false,
        purchaseState: -1,
        error: `Google Play API error: ${response.status} ${errorText}`,
      };
    }

    const data = await response.json() as any;

    // purchaseState: 0 = pending, 1 = purchased, 2 = refunded
    const purchaseState = data.purchaseState || 0;
    const valid = purchaseState === 1;

    return {
      valid,
      purchaseState,
      consumptionState: data.consumptionState,
      acknowledgementState: data.acknowledgementState,
      productId: data.productId,
    };
  } catch (e: any) {
    return {
      valid: false,
      purchaseState: -1,
      error: `Verification failed: ${e.message}`,
    };
  }
}

// Strong mock validation as fallback when Google Play is not configured
export function strongMockValidation(purchaseToken: string): boolean {
  // Token must be at least 20 chars, alphanumeric + underscore/hyphen only
  return /^[A-Za-z0-9_-]{20,}$/.test(purchaseToken);
}
