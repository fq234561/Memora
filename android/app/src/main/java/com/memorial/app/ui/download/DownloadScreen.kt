package com.memorial.app.ui.download

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Regenerate dialog state
    var showRegenerateDialog by remember { mutableStateOf(false) }
    var adjustmentText by remember { mutableStateOf("") }
    var useAdjustment by remember { mutableStateOf(false) }

    LaunchedEffect(saveState) {
        when (saveState) {
            is DownloadViewModel.SaveState.Success -> {
                Toast.makeText(context, "Photo saved to gallery", Toast.LENGTH_SHORT).show()
                viewModel.clearSaveState()
            }
            is DownloadViewModel.SaveState.Error -> {
                val msg = (saveState as DownloadViewModel.SaveState.Error).message
                Toast.makeText(context, "Save failed: $msg", Toast.LENGTH_LONG).show()
                viewModel.clearSaveState()
            }
            else -> {}
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.retryLoad()
        }
    }

    LaunchedEffect(navigateToPreview) {
        if (navigateToPreview) {
            onRegenerate()
            viewModel.onNavigateToPreviewHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Memorial") },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI-generated memorial image",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading your photo...")
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Failed to load",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retryLoad() }) {
                        Text("Retry")
                    }
                }

                else -> {
                    // Display final image
                    photoUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "HD Memorial Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        )
                    } ?: run {
                        androidx.compose.material3.Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("HD Memorial Photo", textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            photoUrl?.let { url ->
                                viewModel.downloadAndSavePhoto(context, url)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = saveState !is DownloadViewModel.SaveState.Saving && photoUrl != null
                    ) {
                        if (saveState is DownloadViewModel.SaveState.Saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Saving...")
                        } else {
                            Text("Download Photo")
                        }
                    }

                    // Regenerate button
                    if (regenerationRemaining > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showRegenerateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Regenerate ($regenerationRemaining left)")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Regenerations exhausted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onBackToHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Home")
                    }
                }
            }
        }
    }

    // Regenerate dialog
    if (showRegenerateDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateDialog = false },
            title = { Text("Regenerate Candidates") },
            text = {
                Column {
                    Text("You have $regenerationRemaining regeneration(s) remaining.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.RadioButton(
                            selected = !useAdjustment,
                            onClick = { useAdjustment = false }
                        )
                        Text("Same settings")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.RadioButton(
                            selected = useAdjustment,
                            onClick = { useAdjustment = true }
                        )
                        Text("Add adjustment")
                    }
                    if (useAdjustment) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adjustmentText,
                            onValueChange = { adjustmentText = it },
                            label = { Text("Describe what to change") },
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
                        val adj = if (useAdjustment && adjustmentText.isNotBlank()) adjustmentText else null
                        viewModel.regenerate(adj)
                        adjustmentText = ""
                        useAdjustment = false
                    },
                    enabled = regenerationRemaining > 0
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
