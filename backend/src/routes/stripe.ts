import { Router, Request, Response } from 'express';
import { authMiddleware } from '../middleware/auth';
import { createCheckoutSession, handleWebhook } from '../services/stripeService';
import { AppError } from '../middleware/errorHandler';

const router = Router();

// POST /api/stripe/create-checkout-session
router.post('/create-checkout-session', authMiddleware, async (req: Request, res: Response) => {
  const { projectId, productId } = req.body as { projectId?: string; productId?: string };
  const userId = req.user!.id;

  if (!projectId || !productId) {
    throw new AppError(400, 'Missing projectId or productId');
  }

  const session = await createCheckoutSession(projectId, productId, userId);

  res.json({
    success: true,
    data: { url: session.url, sessionId: session.id },
  });
});

export default router;

// Stripe webhook handler (used with raw body middleware in app.ts)
export async function stripeWebhookHandler(req: Request, res: Response) {
  const signature = req.headers['stripe-signature'] as string;
  if (!signature) {
    res.status(400).json({ success: false, error: 'Missing stripe-signature header' });
    return;
  }

  try {
    await handleWebhook(req.body, signature);
    res.json({ received: true });
  } catch (err: any) {
    console.error('[stripe] Webhook error:', err.message);
    res.status(400).json({ success: false, error: err.message });
  }
}
