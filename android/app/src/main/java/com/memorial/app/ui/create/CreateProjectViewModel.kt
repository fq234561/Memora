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
                val result = repository.createProject(_title.value, _selectedStyle.value)
                android.util.Log.d("CreateProject", "result: isSuccess=${result.isSuccess}")
                if (result.isSuccess) {
                    onProjectCreated(result.getOrNull()?.id ?: "")
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to create project"
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateProject", "Exception: ${e.message}", e)
                _error.value = e.message ?: "Network error"
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
