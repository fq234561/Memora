package com.memorial.app.ui.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val projectId: String
) : ViewModel() {

    private val repository = ProjectRepository()

    private val _selectedProduct = MutableStateFlow<ProductOption?>(null)
    val selectedProduct: StateFlow<ProductOption?> = _selectedProduct

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing

    private val _purchaseComplete = MutableStateFlow(false)
    val purchaseComplete: StateFlow<Boolean> = _purchaseComplete

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

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
                ProductOption.HD_PHOTO -> "hd_photo"
                ProductOption.PHOTO_AND_VIDEO -> "photo_and_video"
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

            _purchaseComplete.value = true
            _isPurchasing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
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
            else -> msg
        }
    }

    enum class ProductOption(val title: String, val description: String, val price: String) {
        HD_PHOTO(
            "HD Memorial Photo",
            "High-resolution photo without watermark",
            "$9.99"
        ),
        PHOTO_AND_VIDEO(
            "HD Photo + Memorial Video",
            "High-resolution photo + gentle animated memorial video",
            "$14.99"
        )
    }
}
