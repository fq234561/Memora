import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { ApiResponse, Purchase, PurchaseRequest, PurchaseStatus, ProjectStatus, Project } from '../models/types';
import { store } from '../services/mockStore';
import { AppError } from '../middleware/errorHandler';
import { mockAuth } from '../middleware/auth';

const router = Router();

router.use(mockAuth);

// POST /api/purchases - Create a purchase record
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

  // Business logic validation
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

  // Apply product-specific project updates
  const projectUpdates: Partial<Project> = {};

  switch (productId) {
    case 'preview_pack':
      projectUpdates.purchasedProductId = 'preview_pack';
      projectUpdates.regenerationLimit = 0;
      // Status stays UPLOADED so user can click Generate
      break;
    case 'full_pack':
      projectUpdates.purchasedProductId = 'full_pack';
      projectUpdates.regenerationLimit = 2;
      // If candidates already exist and selected, complete immediately
      if (project.candidateUrls && project.selectedCandidateIndex !== undefined) {
        projectUpdates.status = ProjectStatus.COMPLETED;
        projectUpdates.hdPhotoUrl = `https://picsum.photos/seed/${project.id}_hd/800/1200`;
      }
      break;
    case 'hd_unlock':
      // Unlock HD for selected candidate
      projectUpdates.status = ProjectStatus.COMPLETED;
      projectUpdates.hdPhotoUrl = `https://picsum.photos/seed/${project.id}_hd/800/1200`;
      break;
  }

  store.updateProject(projectId, projectUpdates);

  const response: ApiResponse<Purchase> = {
    success: true,
    data: purchase,
  };

  res.status(201).json(response);
});

// POST /api/purchases/verify - Verify purchase with Google Play
router.post('/verify', (req: Request, res: Response) => {
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

  // Mock verification - in production, verify with Google Play Developer API
  const isValid = purchase.purchaseToken.length >= 3;

  if (isValid) {
    store.createPurchase({
      ...purchase,
      status: PurchaseStatus.VERIFIED,
      verifiedAt: new Date().toISOString(),
    });

    // Apply product-specific benefits on verification
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
