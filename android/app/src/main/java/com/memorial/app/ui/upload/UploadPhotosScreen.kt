package com.memorial.app.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

private enum class SelectingSlot {
    NONE, DECEASED, LIVING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPhotosScreen(
    projectId: String,
    onPhotosUploaded: () -> Unit,
    onBack: () -> Unit,
    viewModel: UploadPhotosViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UploadPhotosViewModel(projectId) as T
            }
        }
    )
) {
    val context = LocalContext.current
    val deceasedPhotoUri by viewModel.deceasedPhotoUri.collectAsState()
    val livingPhotoUri by viewModel.livingPhotoUri.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val validationError by viewModel.validationError.collectAsState()
    val uploadSuccess by viewModel.uploadSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectingFor by remember { mutableStateOf(SelectingSlot.NONE) }

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        when (selectingFor) {
            SelectingSlot.DECEASED -> viewModel.onDeceasedPhotoSelected(uri, context)
            SelectingSlot.LIVING -> viewModel.onLivingPhotoSelected(uri, context)
            else -> {}
        }
        selectingFor = SelectingSlot.NONE
    }

    LaunchedEffect(validationError) {
        validationError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uploadSuccess) {
        android.util.Log.d("UploadPhotos", "uploadSuccess changed to: $uploadSuccess")
        if (uploadSuccess) {
            android.util.Log.d("UploadPhotos", "Calling onPhotosUploaded()")
            android.widget.Toast.makeText(context, "Navigating to StyleSelection...", android.widget.Toast.LENGTH_SHORT).show()
            onPhotosUploaded()
        }
    }

    // Dev auto-fill: automatically select test photos and upload on first launch
    LaunchedEffect(Unit) {
        if (deceasedPhotoUri == null && livingPhotoUri == null) {
            // Pass null context to skip validation in dev mode
            viewModel.onDeceasedPhotoSelected(
                android.net.Uri.parse("content://media/external/images/media/1000000050"),
                null
            )
            viewModel.onLivingPhotoSelected(
                android.net.Uri.parse("content://media/external/images/media/1000000051"),
                null
            )
            // Auto-trigger upload after a short delay
            kotlinx.coroutines.delay(500)
            viewModel.uploadPhotos(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Photos") },
                navigationIcon = {
                    OutlinedButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Select reference photos for the AI generation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Quick test photos button for development
            OutlinedButton(
                onClick = {
                    viewModel.onDeceasedPhotoSelected(
                        android.net.Uri.parse("content://media/external/images/media/1000000050"),
                        context
                    )
                    viewModel.onLivingPhotoSelected(
                        android.net.Uri.parse("content://media/external/images/media/1000000051"),
                        context
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🧪 Use Test Photos")
            }

            PhotoSlot(
                label = "Photo of the person you miss",
                photoUri = deceasedPhotoUri,
                onSelect = {
                    selectingFor = SelectingSlot.DECEASED
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            PhotoSlot(
                label = "Photo of yourself or family member",
                photoUri = livingPhotoUri,
                onSelect = {
                    selectingFor = SelectingSlot.LIVING
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { viewModel.uploadPhotos(context) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = deceasedPhotoUri != null && livingPhotoUri != null && !uploadSuccess
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun PhotoSlot(
    label: String,
    photoUri: Uri?,
    onSelect: () -> Unit
) {
    val containerColor = if (photoUri != null) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    androidx.compose.material3.Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = photoUri,
                            contentScale = ContentScale.Crop
                        ),
                        contentDescription = label,
                        modifier = Modifier
                            .size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Selected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Tap to change",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to select from gallery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
