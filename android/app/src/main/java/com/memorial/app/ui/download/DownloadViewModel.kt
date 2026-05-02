package com.memorial.app.ui.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

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

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    init {
        loadProject()
    }

    fun retryLoad() {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getProject(projectId)
            if (result.isSuccess) {
                val project = result.getOrNull()
                _photoUrl.value = project?.hdPhotoUrl ?: project?.generatedPhotoUrl
            } else {
                _errorMessage.value = formatError(result.exceptionOrNull())
            }

            _isLoading.value = false
        }
    }

    fun downloadAndSavePhoto(context: Context, url: String) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val bytes = downloadImageBytes(url)
                val uri = saveImageToGallery(context, bytes)
                if (uri != null) {
                    _saveState.value = SaveState.Success
                } else {
                    _saveState.value = SaveState.Error("Failed to save photo to gallery")
                }
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(formatError(e))
            }
        }
    }

    fun clearSaveState() {
        _saveState.value = SaveState.Idle
    }

    private suspend fun downloadImageBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Download failed: HTTP ${response.code}")
            }
            response.body?.bytes() ?: throw Exception("Empty response body")
        }
    }

    private fun saveImageToGallery(context: Context, bytes: ByteArray): Uri? {
        val contentResolver = context.contentResolver
        val fileName = "memorial_${System.currentTimeMillis()}.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MemorialApp")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return null

        contentResolver.openOutputStream(uri)?.use { output ->
            output.write(bytes)
        } ?: run {
            contentResolver.delete(uri, null, null)
            return null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, values, null, null)
        }

        return uri
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

    sealed class SaveState {
        data object Idle : SaveState()
        data object Saving : SaveState()
        data object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}
