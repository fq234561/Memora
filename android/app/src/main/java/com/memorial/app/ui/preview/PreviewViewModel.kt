package com.memorial.app.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.remote.dto.PromptOptimizeRequest
import com.memorial.app.data.remote.dto.StatusResponse
import com.memorial.app.data.repository.ProjectRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PreviewViewModel(
    private val projectId: String
) : ViewModel() {

    private val repository = ProjectRepository()

    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Loading)
    val uiState: StateFlow<PreviewUiState> = _uiState

    private val _status = MutableStateFlow<String>("UPLOADED")
    val status: StateFlow<String> = _status

    private val _progress = MutableStateFlow<Int?>(null)
    val progress: StateFlow<Int?> = _progress

    private val _candidateUrls = MutableStateFlow<List<String>>(emptyList())
    val candidateUrls: StateFlow<List<String>> = _candidateUrls

    private val _selectedIndex = MutableStateFlow<Int?>(null)
    val selectedIndex: StateFlow<Int?> = _selectedIndex

    private val _regenerationRemaining = MutableStateFlow(0)
    val regenerationRemaining: StateFlow<Int> = _regenerationRemaining

    private val _purchasedProductId = MutableStateFlow<String?>(null)
    val purchasedProductId: StateFlow<String?> = _purchasedProductId

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _onNavigateToPurchase = MutableStateFlow(false)
    val onNavigateToPurchase: StateFlow<Boolean> = _onNavigateToPurchase

    private val _onNavigateToDownload = MutableStateFlow(false)
    val onNavigateToDownload: StateFlow<Boolean> = _onNavigateToDownload

    private var isPolling = false

    init {
        loadProject()
    }

    fun loadProject() {
        viewModelScope.launch {
            _uiState.value = PreviewUiState.Loading
            _errorMessage.value = null

            val projectResult = repository.getProject(projectId)
            if (projectResult.isFailure) {
                _errorMessage.value = formatError(projectResult.exceptionOrNull())
                _uiState.value = PreviewUiState.Error
                return@launch
            }

            val project = projectResult.getOrNull() ?: run {
                _errorMessage.value = "Project not found"
                _uiState.value = PreviewUiState.Error
                return@launch
            }

            _status.value = project.status
            _purchasedProductId.value = project.purchasedProductId
            _regenerationRemaining.value = project.regenerationLimit - project.regenerationCount
            _candidateUrls.value = project.candidateUrls ?: emptyList()
            _selectedIndex.value = project.selectedCandidateIndex

            when (project.status) {
                "PREVIEW_READY" -> {
                    _uiState.value = PreviewUiState.CandidatesReady(
                        project.candidateUrls ?: emptyList(),
                        project.selectedCandidateIndex
                    )
                }
                "GENERATING" -> {
                    _uiState.value = PreviewUiState.Generating(0)
                    startPolling()
                }
                "COMPLETED" -> {
                    _onNavigateToDownload.value = true
                }
                "PURCHASED" -> {
                    _onNavigateToPurchase.value = true
                }
                "FAILED" -> {
                    _uiState.value = if (project.purchasedProductId != null) {
                        PreviewUiState.ReadyToGenerate
                    } else {
                        PreviewUiState.Error
                    }
                }
                else -> {
                    // UPLOADED or DRAFT
                    _uiState.value = if (project.purchasedProductId != null) {
                        PreviewUiState.ReadyToGenerate
                    } else {
                        PreviewUiState.PurchaseRequired
                    }
                }
            }
        }
    }

    fun onPurchaseClicked() {
        _onNavigateToPurchase.value = true
    }

    fun onNavigateToPurchaseHandled() {
        _onNavigateToPurchase.value = false
    }

    fun onNavigateToDownloadHandled() {
        _onNavigateToDownload.value = false
    }

    fun generateCandidates() {
        viewModelScope.launch {
            _uiState.value = PreviewUiState.Loading
            _errorMessage.value = null

            // Build and optimize prompt
            val projectResult = repository.getProject(projectId)
            val project = projectResult.getOrNull()
            val style = project?.style ?: "NATURAL_FAMILY"
            val photoType = style.lowercase()

            val optimizeRequest = PromptOptimizeRequest(
                relationship = "other",
                photoType = photoType,
                style = style,
                mood = "warm"
            )

            val optimizeResult = repository.optimizePrompt(optimizeRequest)
            val customPrompt = if (optimizeResult.isSuccess) {
                optimizeResult.getOrNull()?.optimizedPrompt
            } else null

            val result = repository.generatePhoto(projectId, customPrompt)
            if (result.isFailure) {
                _errorMessage.value = formatError(result.exceptionOrNull())
                _uiState.value = PreviewUiState.Error
                return@launch
            }

            _uiState.value = PreviewUiState.Generating(0)
            startPolling()
        }
    }

    fun regenerateCandidates(adjustmentPrompt: String?) {
        viewModelScope.launch {
            if (_regenerationRemaining.value <= 0) {
                _errorMessage.value = "Regenerations exhausted. Contact support to purchase more."
                return@launch
            }

            _uiState.value = PreviewUiState.Loading
            _errorMessage.value = null

            val result = repository.generatePhoto(
                projectId,
                isRegeneration = true,
                adjustmentPrompt = adjustmentPrompt
            )
            if (result.isFailure) {
                _errorMessage.value = formatError(result.exceptionOrNull())
                _uiState.value = PreviewUiState.Error
                return@launch
            }

            _uiState.value = PreviewUiState.Generating(0)
            startPolling()
        }
    }

    fun selectCandidate(index: Int) {
        _selectedIndex.value = index
        val current = _uiState.value
        if (current is PreviewUiState.CandidatesReady) {
            _uiState.value = PreviewUiState.CandidatesReady(current.candidateUrls, index)
        }
    }

    fun confirmSelection() {
        val index = _selectedIndex.value ?: return
        viewModelScope.launch {
            _uiState.value = PreviewUiState.Loading
            val result = repository.selectCandidate(projectId, index)
            if (result.isFailure) {
                _errorMessage.value = formatError(result.exceptionOrNull())
                _uiState.value = PreviewUiState.CandidatesReady(_candidateUrls.value, index)
                return@launch
            }

            val project = result.getOrNull()
            if (project?.status == "COMPLETED") {
                _onNavigateToDownload.value = true
            } else if (project?.status == "PURCHASED") {
                _onNavigateToPurchase.value = true
            }
        }
    }

    fun retry() {
        _errorMessage.value = null
        loadProject()
    }

    private fun startPolling() {
        if (isPolling) return
        isPolling = true

        viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 60

            while (isPolling && attempts < maxAttempts) {
                attempts++
                delay(2000)

                val result = repository.getStatus(projectId)
                if (result.isSuccess) {
                    val statusData = result.getOrNull()
                    handleStatusUpdate(statusData)

                    if (statusData?.status == "PREVIEW_READY" || statusData?.status == "FAILED") {
                        isPolling = false
                        return@launch
                    }
                } else {
                    val errorMsg = formatError(result.exceptionOrNull())
                    if (errorMsg.contains("Service temporarily unavailable")) {
                        _errorMessage.value = errorMsg
                        _uiState.value = PreviewUiState.Error
                        isPolling = false
                        return@launch
                    }
                }
            }

            if (isPolling) {
                isPolling = false
                _errorMessage.value = "Generation is taking longer than expected. Please check back later."
                _uiState.value = PreviewUiState.Error
            }
        }
    }

    private fun handleStatusUpdate(statusData: StatusResponse?) {
        statusData ?: return

        _status.value = statusData.status
        _progress.value = statusData.progress
        statusData.candidateUrls?.let { _candidateUrls.value = it }
        statusData.regenerationRemaining?.let { _regenerationRemaining.value = it }

        when (statusData.status) {
            "PREVIEW_READY" -> {
                _uiState.value = PreviewUiState.CandidatesReady(
                    statusData.candidateUrls ?: emptyList(),
                    null
                )
            }
            "FAILED" -> {
                _errorMessage.value = "Generation failed. Please try again."
                _uiState.value = if (_purchasedProductId.value != null) {
                    PreviewUiState.ReadyToGenerate
                } else {
                    PreviewUiState.Error
                }
            }
            "GENERATING" -> {
                _uiState.value = PreviewUiState.Generating(statusData.progress ?: 0)
            }
            else -> {
                _uiState.value = PreviewUiState.Generating(0)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isPolling = false
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
            msg.contains("Purchase required", ignoreCase = true) ->
                "Please purchase a Preview Pack or Full Pack to generate candidates."
            msg.contains("Regeneration limit", ignoreCase = true) ->
                "Regenerations exhausted. Contact support to purchase more."
            msg.contains("HD Unlock requires", ignoreCase = true) ->
                "Please purchase Preview Pack or Full Pack first."
            else -> msg
        }
    }

    sealed class PreviewUiState {
        data object Loading : PreviewUiState()
        data object PurchaseRequired : PreviewUiState()
        data object ReadyToGenerate : PreviewUiState()
        data class Generating(val progress: Int) : PreviewUiState()
        data class CandidatesReady(
            val candidateUrls: List<String>,
            val selectedIndex: Int?
        ) : PreviewUiState()
        data object Error : PreviewUiState()
    }
}
