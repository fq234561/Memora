import { Router, Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { ApiResponse, Purchase, PurchaseRequest, PurchaseStatus, ProjectStatus } from '../models/types';
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

  const project = store.getProject(projectId);
  if (!project) {
    throw new AppError(404, 'Project not found');
  }
  if (project.userId !== userId) {
    throw new AppError(403, 'Access denied');
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

  // Update project status to indicate purchase is pending
  store.updateProject(projectId, { status: ProjectStatus.PURCHASED });

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

    // Grant HD access
    const project = store.getProject(purchase.projectId);
    if (project) {
      store.updateProject(project.id, {
        status: ProjectStatus.COMPLETED,
        hdPhotoUrl: `https://picsum.photos/seed/${project.id}_hd/800/1200`,
      });
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
