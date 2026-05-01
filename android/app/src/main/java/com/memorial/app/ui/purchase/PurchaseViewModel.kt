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

            // For MVP Phase 5: Mock purchase token
            // In production, this comes from Google Play Billing Library
            val mockPurchaseToken = "mock_purchase_token_${System.currentTimeMillis()}"
            val mockProductId = when (product) {
                ProductOption.HD_PHOTO -> "hd_photo"
                ProductOption.PHOTO_AND_VIDEO -> "photo_and_video"
            }

            // Create purchase record on backend
            val result = runCatching {
                // Note: Backend purchase/verify endpoints accept PurchaseRequest
                // For mock flow, we simulate a successful verification
                kotlinx.coroutines.delay(1500)
            }

            if (result.isSuccess) {
                _purchaseComplete.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Purchase failed"
            }

            _isPurchasing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
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
