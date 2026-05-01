package com.memorial.app.ui.consent

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConsentViewModel : ViewModel() {

    private val _hasRightToPhotos = MutableStateFlow(true)
    val hasRightToPhotos: StateFlow<Boolean> = _hasRightToPhotos

    private val _privateUseOnly = MutableStateFlow(true)
    val privateUseOnly: StateFlow<Boolean> = _privateUseOnly

    private val _understandAiGenerated = MutableStateFlow(true)
    val understandAiGenerated: StateFlow<Boolean> = _understandAiGenerated

    val allConsentsGiven: Boolean
        get() = hasRightToPhotos.value && privateUseOnly.value && understandAiGenerated.value

    fun toggleHasRightToPhotos() {
        _hasRightToPhotos.value = !_hasRightToPhotos.value
    }

    fun togglePrivateUseOnly() {
        _privateUseOnly.value = !_privateUseOnly.value
    }

    fun toggleUnderstandAiGenerated() {
        _understandAiGenerated.value = !_understandAiGenerated.value
    }
}
