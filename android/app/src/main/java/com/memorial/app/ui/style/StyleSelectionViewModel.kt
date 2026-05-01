package com.memorial.app.ui.style

import androidx.lifecycle.ViewModel
import com.memorial.app.data.model.PhotoStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StyleSelectionViewModel : ViewModel() {

    private val _selectedStyle = MutableStateFlow(PhotoStyle.NATURAL_FAMILY)
    val selectedStyle: StateFlow<PhotoStyle> = _selectedStyle

    fun selectStyle(style: PhotoStyle) {
        _selectedStyle.value = style
    }
}
