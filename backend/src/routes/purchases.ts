import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { ApiResponse, Purchase, PurchaseRequest, PurchaseStatus, ProjectStatus, Project } from '../models/types';
import { store } from '../services/store';
import { AppError } from '../middleware/errorHandler';
import { authMiddleware } from '../middleware/auth';
import { verifyGooglePlayPurchase, strongMockValidation } from '../services/googlePlay';

const router = Router();

router.use(authMiddleware);

const GOOGLE_PLAY_PACKAGE_NAME = process.env.GOOGLE_PLAY_PACKAGE_NAME || 'com.memorial.app';

// POST /api/purchases - Create a purchase record (PENDING only, NO entitlements granted)
router.post('/', (req: Request, res: Response) => {
  const { projectId, productId, purchaseToken } = req.body as PurchaseRequest;
  const userId = req.user!.id;

  if (!projectId || !productId || !purchaseToken) {
    throw new AppError(400, 'Missing required fields');
  }

  const validProducts = ['preview_pack', 'hd_unlock', 'full_pack'];
  if (!validProducts.includes(productId)) {
    throw new AppError(400, 'Invalid productId. Must be preview_pack, hd_unlock, or full_pack');
  }

  const project = store.getProject(projectId);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  // Idempotency: same token cannot be used twice
  const existingByToken = store.getPurchaseByToken(purchaseToken);
  if (existingByToken) {
    const response: ApiResponse<Purchase> = {
      success: true,
      data: existingByToken,
    };
    res.status(200).json(response);
    return;
  }

  // Business logic validation for HD unlock
  if (productId === 'hd_unlock') {
    const hasPreview = project.purchasedProductId === 'preview_pack' || project.purchasedProductId === 'full_pack';
    if (!hasPreview) {
      throw new AppError(400, 'HD Unlock requires Preview Pack or Full Pack first');
    }
    if (!project.candidateUrls || project.selectedCandidateIndex === undefined) {
      throw new AppError(400, 'Please generate and select a candidate image before unlocking HD');
    }
  }

  const purchase: Purchase = {
    id: uuidv4(),
    projectId,
    userId,
    productId,
    purchaseToken,
    status: PurchaseStatus.PENDING,
    createdAt: new Date().toISOString(),
  };

  store.createPurchase(purchase);

  const response: ApiResponse<Purchase> = {
    success: true,
    data: purchase,
  };

  res.status(201).json(response);
});

// POST /api/purchases/verify - Verify purchase and grant entitlements ONLY on success
router.post('/verify', async (req: Request, res: Response) => {
  const { purchaseId } = req.body as { purchaseId?: string };
  const userId = req.user!.id;

  if (!purchaseId) {
    throw new AppError(400, 'Missing purchaseId');
  }

  const purchase = store.getPurchase(purchaseId);
  if (!purchase) {
    throw new AppError(404, 'Purchase not found');
  }
  if (purchase.userId !== userId) {
    throw new AppError(403, 'Access denied');
  }

  // Already verified: return cached result, do NOT re-grant entitlements
  if (purchase.status === PurchaseStatus.VERIFIED) {
    const response: ApiResponse<Purchase> = {
      success: true,
      data: purchase,
    };
    res.status(200).json(response);
    return;
  }

  // Already failed: return failure
  if (purchase.status === PurchaseStatus.FAILED) {
    const response: ApiResponse<Purchase> = {
      success: true,
      data: purchase,
    };
    res.status(200).json(response);
    return;
  }

  let isValid: boolean;

  // Try Google Play Developer API first
  const googlePlayResult = await verifyGooglePlayPurchase(
    GOOGLE_PLAY_PACKAGE_NAME,
    purchase.productId,
    purchase.purchaseToken
  );

  if (googlePlayResult.error && googlePlayResult.error.includes('not configured')) {
    // Fallback: strong mock validation
    console.warn('[purchases] Google Play API not configured. Using strong mock validation.');
    isValid = strongMockValidation(purchase.purchaseToken);
  } else if (googlePlayResult.error) {
    // Google Play API error
    console.error('[purchases] Google Play verification error:', googlePlayResult.error);
    isValid = false;
  } else {
    // Google Play API success
    isValid = googlePlayResult.valid && googlePlayResult.purchaseState === 1;
    if (!isValid) {
      console.warn('[purchases] Google Play purchase invalid:', {
        purchaseState: googlePlayResult.purchaseState,
        productId: googlePlayResult.productId,
      });
    }
  }

  if (isValid) {
    store.createPurchase({
      ...purchase,
      status: PurchaseStatus.VERIFIED,
      verifiedAt: new Date().toISOString(),
    });

    // Grant entitlements ONLY NOW after successful verification
    const project = store.getProject(purchase.projectId);
    if (project) {
      const projectUpdates: Partial<Project> = {};

      switch (purchase.productId) {
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

      store.updateProject(project.id, projectUpdates);
    }
  } else {
    store.createPurchase({
      ...purchase,
      status: PurchaseStatus.FAILED,
    });
  }

  const updatedPurchase = store.getPurchase(purchaseId);

  const response: ApiResponse<Purchase> = {
    success: true,
    data: updatedPurchase!,
  };

  res.status(200).json(response);
});

export default router;
