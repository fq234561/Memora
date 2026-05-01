package com.memorial.app.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            val result = repository.generatePhoto(projectId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to start generation"
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
                    }
                }
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

    sealed class PreviewUiState {
        data object Loading : PreviewUiState()
        data class Generating(val progress: Int) : PreviewUiState()
        data class Success(val previewUrl: String?) : PreviewUiState()
        data object Error : PreviewUiState()
    }
}
