package com.memorial.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.model.PhotoStyle
import com.memorial.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateProjectViewModel : ViewModel() {

    private val repository = ProjectRepository()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _selectedStyle = MutableStateFlow(PhotoStyle.NATURAL_FAMILY)
    val selectedStyle: StateFlow<PhotoStyle> = _selectedStyle

    private val _eventDate = MutableStateFlow("")
    val eventDate: StateFlow<String> = _eventDate

    private val _selectedActivityType = MutableStateFlow<com.memorial.app.data.model.ActivityType?>(null)
    val selectedActivityType: StateFlow<com.memorial.app.data.model.ActivityType?> = _selectedActivityType

    private val _selectedPersonTypes = MutableStateFlow<List<com.memorial.app.data.model.PersonType>>(emptyList())
    val selectedPersonTypes: StateFlow<List<com.memorial.app.data.model.PersonType>> = _selectedPersonTypes

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onStyleSelected(style: PhotoStyle) {
        _selectedStyle.value = style
    }

    fun onEventDateChange(date: String) { _eventDate.value = date }
    fun onActivitySelected(type: com.memorial.app.data.model.ActivityType?) { _selectedActivityType.value = type }
    fun togglePersonType(type: com.memorial.app.data.model.PersonType) {
        _selectedPersonTypes.value = if (type in _selectedPersonTypes.value) {
            _selectedPersonTypes.value - type
        } else {
            _selectedPersonTypes.value + type
        }
    }

    fun createProject(onProjectCreated: (String) -> Unit) {
        android.util.Log.d("CreateProject", "createProject called, title='${_title.value}'")
        if (_title.value.isBlank()) {
            android.util.Log.d("CreateProject", "title is blank, returning")
            return
        }

        viewModelScope.launch {
            _isCreating.value = true
            _error.value = null
            try {
                android.util.Log.d("CreateProject", "calling repository.createProject")
                val result = repository.createProject(
                    title = _title.value,
                    style = _selectedStyle.value,
                    eventDate = _eventDate.value.ifBlank { null },
                    activityType = _selectedActivityType.value?.name,
                    personTypes = _selectedPersonTypes.value.map { it.name }.ifEmpty { null }
                )
                android.util.Log.d("CreateProject", "result: isSuccess=${result.isSuccess}")
                if (result.isSuccess) {
                    onProjectCreated(result.getOrNull()?.id ?: "")
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to create project"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create project"
            } finally {
                _isCreating.value = false
            }
        }
    }
}
