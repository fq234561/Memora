package com.memorial.app.ui.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UploadPhotosViewModel(
    private val projectId: String
) : ViewModel() {

    private val repository = ProjectRepository()

    private val _deceasedPhotoUri = MutableStateFlow<Uri?>(null)
    val deceasedPhotoUri: StateFlow<Uri?> = _deceasedPhotoUri

    private val _livingPhotoUri = MutableStateFlow<Uri?>(null)
    val livingPhotoUri: StateFlow<Uri?> = _livingPhotoUri

    init {
        // Dev auto-fill: pre-populate with mock URIs and auto-upload
        _deceasedPhotoUri.value = android.net.Uri.parse("content://media/external/images/media/1000000050")
        _livingPhotoUri.value = android.net.Uri.parse("content://media/external/images/media/1000000051")
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _uploadSuccess.value = true
        }
    }

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess

    companion object {
        private const val MIN_FILE_SIZE_BYTES = 10 * 1024L
        private const val MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024L
        private const val MIN_DIMENSION = 256
        private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")
    }

    fun onDeceasedPhotoSelected(uri: Uri?, context: Context?) {
        val error = validateImage(uri, context)
        if (error != null) {
            _validationError.value = error
            return
        }
        _deceasedPhotoUri.value = uri
        _validationError.value = null
    }

    fun onLivingPhotoSelected(uri: Uri?, context: Context?) {
        val error = validateImage(uri, context)
        if (error != null) {
            _validationError.value = error
            return
        }
        _livingPhotoUri.value = uri
        _validationError.value = null
    }

    fun clearError() {
        _validationError.value = null
    }

    fun uploadPhotos(context: Context) {
        val deceasedUri = _deceasedPhotoUri.value
        val livingUri = _livingPhotoUri.value

        if (deceasedUri == null || livingUri == null) {
            _validationError.value = "Please select both photos"
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            _validationError.value = null

            // Dev mode: skip actual upload and simulate success
            kotlinx.coroutines.delay(1000)
            _uploadSuccess.value = true
            _isUploading.value = false
        }
    }

    private fun validateImage(uri: Uri?, context: Context?): String? {
        if (uri == null) return "No image selected"
        if (context == null) return null

        val contentResolver = context.contentResolver

        val mimeType = contentResolver.getType(uri)
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.lowercase())) {
            return "Only JPG, PNG, and WebP images are supported"
        }

        var sizeBytes: Long? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }

        if (sizeBytes == null) {
            try {
                contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                    sizeBytes = fd.statSize
                }
            } catch (_: Exception) {}
        }

        sizeBytes?.let { size ->
            if (size < MIN_FILE_SIZE_BYTES) return "Image is too small (minimum 10KB)"
            if (size > MAX_FILE_SIZE_BYTES) return "Image is too large (maximum 20MB)"
        }

        try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream, null, options)
            }
            if (options.outWidth > 0 && options.outHeight > 0) {
                if (options.outWidth < MIN_DIMENSION || options.outHeight < MIN_DIMENSION) {
                    return "Image resolution is too low (minimum 256x256 pixels)"
                }
            }
        } catch (_: Exception) {
            return "Unable to read image. The file may be corrupted."
        }

        return null
    }
}
