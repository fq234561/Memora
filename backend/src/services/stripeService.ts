import Stripe from 'stripe';
import { env } from '../utils/env';
import { store } from './store';
import { Purchase, PurchaseStatus, ProjectStatus } from '../models/types';

const stripeClient = env.STRIPE_SECRET_KEY
  ? new Stripe(env.STRIPE_SECRET_KEY, { apiVersion: '2025-04-30.basil' as any })
  : null;

function getStripe() {
  if (!stripeClient) {
    throw new Error('Stripe is not configured. Set STRIPE_SECRET_KEY.');
  }
  return stripeClient;
}

export function getPriceId(productId: string): string {
  switch (productId) {
    case 'preview_pack':
      return env.STRIPE_PRICE_PREVIEW_PACK;
    case 'hd_unlock':
      return env.STRIPE_PRICE_HD_UNLOCK;
    case 'full_pack':
      return env.STRIPE_PRICE_FULL_PACK;
    default:
      throw new Error(`Unknown productId: ${productId}`);
  }
}

export async function createCheckoutSession(
  projectId: string,
  productId: string,
  userId: string
): Promise<any> {
  const s = getStripe();
  const priceId = getPriceId(productId);

  const session = await s.checkout.sessions.create({
    payment_method_types: ['card'],
    line_items: [{ price: priceId, quantity: 1 }],
    mode: 'payment',
    success_url: `${env.WEB_APP_URL}/download/${projectId}?session_id={CHECKOUT_SESSION_ID}`,
    cancel_url: `${env.WEB_APP_URL}/preview/${projectId}`,
    metadata: {
      projectId,
      userId,
      productId,
    },
  });

  return session;
}

export async function handleWebhook(payload: string | Buffer, signature: string): Promise<any> {
  const s = getStripe();
  const event = s.webhooks.constructEvent(payload, signature, env.STRIPE_WEBHOOK_SECRET);

  if (event.type === 'checkout.session.completed') {
    const session = event.data.object as any;
    await fulfillCheckout(session);
  }

  return event;
}

async function fulfillCheckout(session: any) {
  const { projectId, userId, productId } = session.metadata || {};
  if (!projectId || !userId || !productId) {
    console.warn('[stripe] Missing metadata in checkout session:', session.id);
    return;
  }

  // Idempotency: check if already fulfilled
  const existing = await store.getPurchaseByToken(session.id);
  if (existing && existing.status === PurchaseStatus.VERIFIED) {
    console.log('[stripe] Purchase already fulfilled:', session.id);
    return;
  }

  // Create or update purchase record
  const purchase: Purchase = existing || {
    id: session.id,
    projectId,
    userId,
    productId,
    purchaseToken: session.id,
    status: PurchaseStatus.PENDING,
    provider: 'stripe',
    stripeSessionId: session.id,
    stripePaymentIntentId: session.payment_intent as string,
    createdAt: new Date().toISOString(),
  };

  purchase.status = PurchaseStatus.VERIFIED;
  purchase.verifiedAt = new Date().toISOString();
  purchase.provider = 'stripe';
  purchase.stripeSessionId = session.id;
  purchase.stripePaymentIntentId = session.payment_intent as string;

  await store.createPurchase(purchase);

  // Grant entitlements
  const project = await store.getProject(projectId);
  if (project) {
    const projectUpdates: Partial<typeof project> = {};

    switch (productId) {
      case 'preview_pack':
        projectUpdates.purchasedProductId = 'preview_pack';
        projectUpdates.regenerationLimit = 0;
        break;
      case 'full_pack':
        projectUpdates.purchasedProductId = 'full_pack';
        projectUpdates.regenerationLimit = 2;
        if (project.candidateUrls && project.selectedCandidateIndex !== undefined) {
          projectUpdates.status = ProjectStatus.COMPLETED;
          projectUpdates.hdPhotoUrl = `https://picsum.photos/seed/${project.id}_hd/800/1200`;
        }
        break;
      case 'hd_unlock':
        projectUpdates.status = ProjectStatus.COMPLETED;
        projectUpdates.hdPhotoUrl = `https://picsum.photos/seed/${project.id}_hd/800/1200`;
        break;
    }

    await store.updateProject(projectId, projectUpdates);
  }
}
