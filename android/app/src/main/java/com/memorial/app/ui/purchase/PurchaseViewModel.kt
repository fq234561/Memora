package com.memorial.app.ui.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.remote.dto.ProjectDto
import com.memorial.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val projectId: String
) : ViewModel() {

    private val repository = ProjectRepository()

    private val _availableProducts = MutableStateFlow<List<ProductOption>>(emptyList())
    val availableProducts: StateFlow<List<ProductOption>> = _availableProducts

    private val _selectedProduct = MutableStateFlow<ProductOption?>(null)
    val selectedProduct: StateFlow<ProductOption?> = _selectedProduct

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing

    private val _purchaseComplete = MutableStateFlow(false)
    val purchaseComplete: StateFlow<Boolean> = _purchaseComplete

    private val _navigateToDownload = MutableStateFlow(false)
    val navigateToDownload: StateFlow<Boolean> = _navigateToDownload

    private val _navigateToPreview = MutableStateFlow(false)
    val navigateToPreview: StateFlow<Boolean> = _navigateToPreview

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var project: ProjectDto? = null

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            val result = repository.getProject(projectId)
            if (result.isSuccess) {
                project = result.getOrNull()
                determineAvailableProducts()
            } else {
                _errorMessage.value = formatError(result.exceptionOrNull())
            }
        }
    }

    private fun determineAvailableProducts() {
        val purchased = project?.purchasedProductId
        val products = when (purchased) {
            null -> listOf(ProductOption.PREVIEW_PACK, ProductOption.FULL_PACK)
            "preview_pack" -> listOf(ProductOption.HD_UNLOCK)
            "full_pack" -> emptyList() // Already owns everything
            else -> listOf(ProductOption.PREVIEW_PACK, ProductOption.FULL_PACK)
        }
        _availableProducts.value = products
        _selectedProduct.value = products.firstOrNull()
    }

    fun selectProduct(product: ProductOption) {
        _selectedProduct.value = product
        _errorMessage.value = null
    }

    fun purchase() {
        val product = _selectedProduct.value ?: return

        viewModelScope.launch {
            _isPurchasing.value = true
            _errorMessage.value = null

            val mockPurchaseToken = "mock_purchase_token_${System.currentTimeMillis()}"
            val mockProductId = when (product) {
                ProductOption.PREVIEW_PACK -> "preview_pack"
                ProductOption.HD_UNLOCK -> "hd_unlock"
                ProductOption.FULL_PACK -> "full_pack"
            }

            // Step 1: Create purchase record on backend
            val createResult = repository.createPurchase(projectId, mockProductId, mockPurchaseToken)
            if (createResult.isFailure) {
                _errorMessage.value = formatError(createResult.exceptionOrNull())
                _isPurchasing.value = false
                return@launch
            }

            val purchaseId = createResult.getOrNull()?.id ?: run {
                _errorMessage.value = "Purchase created but no ID returned"
                _isPurchasing.value = false
                return@launch
            }

            // Step 2: Verify purchase (mock Google Play verification)
            val verifyResult = repository.verifyPurchase(purchaseId)
            if (verifyResult.isFailure) {
                _errorMessage.value = formatError(verifyResult.exceptionOrNull())
                _isPurchasing.value = false
                return@launch
            }

            // Determine navigation based on product and project state
            val verifiedPurchase = verifyResult.getOrNull()
            val updatedProject = repository.getProject(projectId).getOrNull()

            when (verifiedPurchase?.productId) {
                "preview_pack" -> {
                    _navigateToPreview.value = true
                }
                "hd_unlock", "full_pack" -> {
                    if (updatedProject?.status == "COMPLETED") {
                        _navigateToDownload.value = true
                    } else {
                        _navigateToPreview.value = true
                    }
                }
            }

            _purchaseComplete.value = true
            _isPurchasing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun onNavigateHandled() {
        _navigateToDownload.value = false
        _navigateToPreview.value = false
    }

    private fun formatError(error: Throwable?): String {
        val msg = error?.message ?: "Unknown error"
        return when {
            msg.contains("connect", ignoreCase = true) ||
            msg.contains("timeout", ignoreCase = true) ||
            msg.contains("unable to resolve", ignoreCase = true) ||
            msg.contains("connection", ignoreCase = true) ||
            msg.contains("refused", ignoreCase = true) ||
            msg.contains("CLEARTEXT", ignoreCase = true) ->
                "Service temporarily unavailable. Please try again later."
            msg.contains("requires Preview Pack", ignoreCase = true) ->
                "Please purchase Preview Pack or Full Pack first."
            else -> msg
        }
    }

    enum class ProductOption(val productId: String, val title: String, val description: String, val price: String) {
        PREVIEW_PACK(
            "preview_pack",
            "Preview Pack",
            "Generate 4 AI candidate images with watermark",
            "$2.99"
        ),
        HD_UNLOCK(
            "hd_unlock",
            "HD Unlock",
            "Unlock high-resolution version of your selected image",
            "$6.99"
        ),
        FULL_PACK(
            "full_pack",
            "Full Pack",
            "4 candidates + 1 HD image + 2 regenerations",
            "$12.99"
        )
    }
}
