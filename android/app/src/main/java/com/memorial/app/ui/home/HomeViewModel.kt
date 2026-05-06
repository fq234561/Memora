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

    enum class FilterType { ALL, YEAR_MONTH, ACTIVITY, PERSON }

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

    // Filter states
    private val _selectedFilter = MutableStateFlow(FilterType.ALL)
    val selectedFilter: StateFlow<FilterType> = _selectedFilter

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear

    private val _selectedMonth = MutableStateFlow<Int?>(null)
    val selectedMonth: StateFlow<Int?> = _selectedMonth

    private val _selectedActivityType = MutableStateFlow<String?>(null)
    val selectedActivityType: StateFlow<String?> = _selectedActivityType

    private val _selectedPersonType = MutableStateFlow<String?>(null)
    val selectedPersonType: StateFlow<String?> = _selectedPersonType

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
        val filter = _selectedFilter.value
        val result = when (filter) {
            FilterType.YEAR_MONTH -> projectRepository.getProjects(
                year = _selectedYear.value,
                month = _selectedMonth.value
            )
            FilterType.ACTIVITY -> projectRepository.getProjects(
                activityType = _selectedActivityType.value
            )
            FilterType.PERSON -> projectRepository.getProjects(
                personType = _selectedPersonType.value
            )
            else -> projectRepository.getProjects()
        }
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

    fun onFilterSelected(filter: FilterType) { _selectedFilter.value = filter }
    fun onYearSelected(year: Int?) { _selectedYear.value = year }
    fun onMonthSelected(month: Int?) { _selectedMonth.value = month }
    fun onActivityTypeFilter(activityType: String?) { _selectedActivityType.value = activityType }
    fun onPersonTypeFilter(personType: String?) { _selectedPersonType.value = personType }

    private fun ProjectDto.toModel(): Project {
        return Project(
            id = id,
            title = title,
            style = com.memorial.app.data.model.PhotoStyle.valueOf(style),
            status = com.memorial.app.data.model.ProjectStatus.valueOf(status),
            createdAt = createdAt,
            updatedAt = updatedAt,
            generatedPhotoUrl = generatedPhotoUrl,
            hdPhotoUrl = hdPhotoUrl,
            candidateUrls = candidateUrls,
            regenerationCount = regenerationCount,
            regenerationLimit = regenerationLimit,
            purchasedProductId = purchasedProductId,
            eventDate = eventDate,
            activityType = activityType,
            personTypes = personTypes,
            detectedTags = detectedTags,
            albumId = albumId
        )
    }
}
