package com.memorial.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.local.TokenManager
import com.memorial.app.data.model.Project
import com.memorial.app.data.remote.dto.ProjectDto
import com.memorial.app.data.repository.AuthRepository
import com.memorial.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(TokenManager(application))
    private val projectRepository = ProjectRepository()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _userName = MutableStateFlow(authRepository.getUserName())
    val userName: StateFlow<String> = _userName

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        android.util.Log.e("HomeViewModel", "init called")
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            android.util.Log.e("HomeViewModel", "loadProjects started")
            doLoad()
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            doLoad()
            _isRefreshing.value = false
        }
    }

    private suspend fun doLoad() {
        val result = projectRepository.getProjects()
        android.util.Log.e("HomeViewModel", "loadProjects result: isSuccess=${result.isSuccess}, count=${result.getOrDefault(emptyList()).size}")
        if (result.isSuccess) {
            _projects.value = result.getOrDefault(emptyList()).map { it.toModel() }
        } else {
            _error.value = result.exceptionOrNull()?.message
            android.util.Log.e("HomeViewModel", "loadProjects error: ${_error.value}")
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun ProjectDto.toModel(): Project {
        return Project(
            id = id,
            title = title,
            style = com.memorial.app.data.model.PhotoStyle.valueOf(style),
            status = com.memorial.app.data.model.ProjectStatus.valueOf(status),
            createdAt = createdAt,
            updatedAt = updatedAt,
            generatedPhotoUrl = generatedPhotoUrl,
            hdPhotoUrl = hdPhotoUrl
        )
    }
}
