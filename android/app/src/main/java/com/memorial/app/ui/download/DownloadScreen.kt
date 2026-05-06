package com.memorial.app.ui.download

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.memorial.app.ui.theme.BackgroundWarm
import com.memorial.app.ui.theme.CardSurface
import com.memorial.app.ui.theme.DividerLight
import com.memorial.app.ui.theme.PrimaryGreen
import com.memorial.app.ui.theme.TextMuted
import com.memorial.app.ui.theme.TextPrimary
import com.memorial.app.ui.theme.TextSecondary

@Composable
fun DownloadScreen(
    projectId: String,
    onBackToHome: () -> Unit,
    onRegenerate: () -> Unit,
    viewModel: DownloadViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DownloadViewModel(projectId) as T
            }
        }
    )
) {
    val context = LocalContext.current
    val photoUrl by viewModel.photoUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val regenerationRemaining by viewModel.regenerationRemaining.collectAsState()
    val navigateToPreview by viewModel.navigateToPreview.collectAsState()
    var showRegenerateDialog by remember { mutableStateOf(false) }
    var useAdjustment by remember { mutableStateOf(false) }
    var adjustmentText by remember { mutableStateOf("") }

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is DownloadViewModel.SaveState.Success -> {
                Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
                viewModel.clearSaveState()
            }
            is DownloadViewModel.SaveState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.clearSaveState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(navigateToPreview) {
        if (navigateToPreview) {
            viewModel.onNavigateToPreviewHandled()
            onRegenerate()
        }
    }

    Scaffold(containerColor = BackgroundWarm) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Family Memory Photo",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onBackToHome) {
                    Text("Home", color = TextSecondary)
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardSurface)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage ?: "Unable to load this project",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.retryLoad() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                photoUrl != null -> {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Generated family memory photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardSurface),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = "AI-generated family memory photo",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )

                    Button(
                        onClick = { viewModel.downloadAndSavePhoto(context, photoUrl!!) },
                        enabled = saveState !is DownloadViewModel.SaveState.Saving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(
                            text = if (saveState is DownloadViewModel.SaveState.Saving) "Saving..." else "Save HD Photo",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    if (regenerationRemaining > 0) {
                        OutlinedButton(
                            onClick = { showRegenerateDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Regenerate ($regenerationRemaining left)")
                        }
                    }
                }

                else -> {
                    Text(
                        text = "No generated photo is ready yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            }

            OutlinedButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to Home")
            }
        }
    }

    if (showRegenerateDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateDialog = false },
            title = { Text("Regenerate Options") },
            text = {
                Column {
                    Text("You have $regenerationRemaining regeneration opportunities left.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !useAdjustment,
                            onClick = { useAdjustment = false }
                        )
                        Text("Use the same settings")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = useAdjustment,
                            onClick = { useAdjustment = true }
                        )
                        Text("Add adjustment notes")
                    }
                    if (useAdjustment) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adjustmentText,
                            onValueChange = { adjustmentText = it },
                            label = { Text("Describe the adjustment you want") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRegenerateDialog = false
                        val adjustment = if (useAdjustment && adjustmentText.isNotBlank()) adjustmentText else null
                        viewModel.regenerate(adjustment)
                        adjustmentText = ""
                        useAdjustment = false
                    },
                    enabled = regenerationRemaining > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
