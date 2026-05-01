package com.memorial.app.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val projectId: String
) : ViewModel() {

    private val repository = ProjectRepository()

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getProject(projectId)
            if (result.isSuccess) {
                val project = result.getOrNull()
                _photoUrl.value = project?.generatedPhotoUrl ?: project?.hdPhotoUrl
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load project"
            }

            _isLoading.value = false
        }
    }
}
