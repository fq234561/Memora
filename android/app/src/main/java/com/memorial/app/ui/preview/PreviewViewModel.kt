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

    private val _status = MutableStateFlow<String>("GENERATING")
    val status: StateFlow<String> = _status

    private val _progress = MutableStateFlow<Int?>(null)
    val progress: StateFlow<Int?> = _progress

    private val _resultUrl = MutableStateFlow<String?>(null)
    val resultUrl: StateFlow<String?> = _resultUrl

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var isPolling = false

    init {
        startGeneration()
    }

    private fun startGeneration() {
        viewModelScope.launch {
            _uiState.value = PreviewUiState.Loading
            _errorMessage.value = null

            // Check current project status first to avoid re-triggering
            val projectResult = repository.getProject(projectId)
            if (projectResult.isSuccess) {
                val project = projectResult.getOrNull()
                when (project?.status) {
                    "PREVIEW_READY", "COMPLETED" -> {
                        _resultUrl.value = project.generatedPhotoUrl
                        _uiState.value = PreviewUiState.Success(project.generatedPhotoUrl)
                        return@launch
                    }
                    "GENERATING" -> {
                        _uiState.value = PreviewUiState.Generating(0)
                        startPolling()
                        return@launch
                    }
                    else -> { /* continue to trigger generation */ }
                }
            } else {
                _errorMessage.value = formatError(projectResult.exceptionOrNull())
                _uiState.value = PreviewUiState.Error
                return@launch
            }

            // Build and optimize prompt using project style
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
            } else {
                null // Fall back to default generation if optimization fails
            }

            val result = repository.generatePhoto(projectId, customPrompt)
            if (result.isFailure) {
                _errorMessage.value = formatError(result.exceptionOrNull())
                _uiState.value = PreviewUiState.Error
                return@launch
            }
            // Start polling status
            startPolling()
        }
    }

    private fun startPolling() {
        if (isPolling) return
        isPolling = true

        viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 60 // Poll for up to 60 attempts (approx 2 minutes)

            while (isPolling && attempts < maxAttempts) {
                attempts++
                delay(2000) // Poll every 2 seconds

                val result = repository.getStatus(projectId)
                if (result.isSuccess) {
                    val statusData = result.getOrNull()
                    handleStatusUpdate(statusData)

                    // Stop polling if terminal state reached
                    if (statusData?.status == "PREVIEW_READY" || statusData?.status == "FAILED") {
                        isPolling = false
                        return@launch
                    }
                } else {
                    // Network/backend error during polling
                    val errorMsg = formatError(result.exceptionOrNull())
                    if (errorMsg.contains("Service temporarily unavailable")) {
                        _errorMessage.value = errorMsg
                        _uiState.value = PreviewUiState.Error
                        isPolling = false
                        return@launch
                    }
                    // For other errors, keep polling but log it
                }
            }

            // Timeout: max attempts reached without terminal state
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

        when (statusData.status) {
            "PREVIEW_READY" -> {
                _resultUrl.value = statusData.resultUrl
                _uiState.value = PreviewUiState.Success(statusData.resultUrl)
            }
            "FAILED" -> {
                _errorMessage.value = "Generation failed. Please try again."
                _uiState.value = PreviewUiState.Error
            }
            "GENERATING" -> {
                _uiState.value = PreviewUiState.Generating(
                    statusData.progress ?: 0
                )
            }
            else -> {
                _uiState.value = PreviewUiState.Generating(0)
            }
        }
    }

    fun retry() {
        _errorMessage.value = null
        _uiState.value = PreviewUiState.Loading
        startGeneration()
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
            else -> msg
        }
    }

    sealed class PreviewUiState {
        data object Loading : PreviewUiState()
        data class Generating(val progress: Int) : PreviewUiState()
        data class Success(val previewUrl: String?) : PreviewUiState()
        data object Error : PreviewUiState()
    }
}
