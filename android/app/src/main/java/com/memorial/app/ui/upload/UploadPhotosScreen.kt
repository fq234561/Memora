package com.memorial.app.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorial.app.BuildConfig
import coil.compose.rememberAsyncImagePainter
import com.memorial.app.ui.theme.BackgroundWarm
import com.memorial.app.ui.theme.CardSurface
import com.memorial.app.ui.theme.DividerLight
import com.memorial.app.ui.theme.PrimaryGreen
import com.memorial.app.ui.theme.PrimaryGreenLight
import com.memorial.app.ui.theme.TextMuted
import com.memorial.app.ui.theme.TextPrimary
import com.memorial.app.ui.theme.TextSecondary
import com.memorial.app.ui.theme.TrustGreen

private enum class SelectingSlot {
    NONE, BASE, PERSON
}

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
    val basePhotoUri by viewModel.basePhotoUri.collectAsState()
    val personPhotoUri by viewModel.personPhotoUri.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val validationError by viewModel.validationError.collectAsState()
    val uploadSuccess by viewModel.uploadSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectingFor by remember { mutableStateOf(SelectingSlot.NONE) }

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        when (selectingFor) {
            SelectingSlot.BASE -> viewModel.onBasePhotoSelected(uri, context)
            SelectingSlot.PERSON -> viewModel.onPersonPhotoSelected(uri, context)
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
        if (uploadSuccess) {
            onPhotosUploaded()
        }
    }

    // Dev auto-fill
    LaunchedEffect(Unit) {
        if (basePhotoUri != null && personPhotoUri != null && !uploadSuccess) {
            kotlinx.coroutines.delay(500)
            viewModel.uploadPhotos(context)
        }
    }

    Scaffold(
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upload Photos",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary
                        )
                    )
                }
            }

            Text(
                text = "Select photos and let AI complete a family memory photo",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )

            // Privacy notice
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = PrimaryGreenLight,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Your photos are only used to generate family photos and will be securely deleted after processing is complete",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (BuildConfig.ENABLE_MOCK_AUTH) {
                // Dev quick fill - subtle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.onBasePhotoSelected(
                                android.net.Uri.parse("file:///sdcard/Pictures/test_normal.jpg"),
                                context
                            )
                            viewModel.onPersonPhotoSelected(
                                android.net.Uri.parse("file:///sdcard/Pictures/test_living.jpg"),
                                context
                            )
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Use test photos (Dev)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted
                        )
                    )
                }
            }

            // Photo slots
            PhotoSlotCard(
                label = "Scene Photo",
                subtitle = "Choose an event photo or scenery shot",
                photoUri = basePhotoUri,
                onSelect = {
                    selectingFor = SelectingSlot.BASE
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            PhotoSlotCard(
                label = "Person Reference",
                subtitle = "Choose the reference photo of the person to blend in",
                photoUri = personPhotoUri,
                onSelect = {
                    selectingFor = SelectingSlot.PERSON
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = PrimaryGreen
                )
            } else {
                Button(
                    onClick = { viewModel.uploadPhotos(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = basePhotoUri != null && personPhotoUri != null && !uploadSuccess,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = DividerLight
                    )
                ) {
                    Text(
                        "Continue",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PhotoSlotCard(
    label: String,
    subtitle: String,
    photoUri: Uri?,
    onSelect: () -> Unit
) {
    val isFilled = photoUri != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isFilled) CardSurface else BackgroundWarm)
            .clickable { onSelect() }
    ) {
        if (isFilled) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = photoUri,
                        contentScale = ContentScale.Crop
                    ),
                    contentDescription = label,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TrustGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Selected - Tap to change",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary
                        )
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted
                    )
                )
            }
        }
    }
}
