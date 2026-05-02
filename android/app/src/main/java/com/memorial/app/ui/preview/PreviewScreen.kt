package com.memorial.app.ui.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    projectId: String,
    onProceedToPurchase: () -> Unit,
    onNavigateToDownload: () -> Unit,
    onBack: () -> Unit,
    viewModel: PreviewViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PreviewViewModel(projectId) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val candidateUrls by viewModel.candidateUrls.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val regenerationRemaining by viewModel.regenerationRemaining.collectAsState()
    val purchasedProductId by viewModel.purchasedProductId.collectAsState()
    val onNavigateToPurchase by viewModel.onNavigateToPurchase.collectAsState()
    val onNavigateToDownload by viewModel.onNavigateToDownload.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation effects
    LaunchedEffect(onNavigateToPurchase) {
        if (onNavigateToPurchase) {
            onProceedToPurchase()
            viewModel.onNavigateToPurchaseHandled()
        }
    }

    LaunchedEffect(onNavigateToDownload) {
        if (onNavigateToDownload) {
            onNavigateToDownload()
            viewModel.onNavigateToDownloadHandled()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.retry()
        }
    }

    // Regenerate dialog state
    var showRegenerateDialog by remember { mutableStateOf(false) }
    var adjustmentText by remember { mutableStateOf("") }
    var useAdjustment by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is PreviewViewModel.PreviewUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PreviewViewModel.PreviewUiState.PurchaseRequired -> {
                    PurchaseRequiredContent(
                        onPurchase = { viewModel.onPurchaseClicked() }
                    )
                }

                is PreviewViewModel.PreviewUiState.ReadyToGenerate -> {
                    ReadyToGenerateContent(
                        onGenerate = { viewModel.generateCandidates() }
                    )
                }

                is PreviewViewModel.PreviewUiState.Generating -> {
                    GeneratingContent(progress = progress)
                }

                is PreviewViewModel.PreviewUiState.CandidatesReady -> {
                    CandidatesContent(
                        candidateUrls = candidateUrls,
                        selectedIndex = selectedIndex,
                        regenerationRemaining = regenerationRemaining,
                        purchasedProductId = purchasedProductId,
                        onSelect = { viewModel.selectCandidate(it) },
                        onRegenerate = { showRegenerateDialog = true },
                        onConfirm = { viewModel.confirmSelection() }
                    )
                }

                is PreviewViewModel.PreviewUiState.Error -> {
                    ErrorContent(
                        errorMessage = errorMessage,
                        hasPurchase = purchasedProductId != null,
                        onRetry = { viewModel.retry() }
                    )
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
                        viewModel.regenerateCandidates(adj)
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

@Composable
private fun PurchaseRequiredContent(
    onPurchase: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Generate AI Memorial Photo",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Purchase a Preview Pack to generate 4 AI candidate images.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onPurchase,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Purchase Preview Pack ($2.99)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onPurchase,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Get Full Pack ($12.99)")
        }
    }
}

@Composable
private fun ReadyToGenerateContent(
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ready to Generate",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your photos and style preferences are ready. Generate 4 AI candidate images now.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Generate 4 Candidates")
        }
    }
}

@Composable
private fun GeneratingContent(progress: Int?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Generating your memorial photo candidates...",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            progress?.let {
                Text("$it%", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { it / 100f },
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
        }
    }
}

@Composable
private fun CandidatesContent(
    candidateUrls: List<String>,
    selectedIndex: Int?,
    regenerationRemaining: Int,
    purchasedProductId: String?,
    onSelect: (Int) -> Unit,
    onRegenerate: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Choose your favorite",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Tap to select one candidate image",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 Grid of candidates
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(candidateUrls) { index, url ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .aspectRatio(3f / 4f)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onSelect(index) }
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Candidate ${index + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(28.dp)
                            )
                        }
                    }
                    // Watermark overlay
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = "PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Regenerate button
        if (purchasedProductId == "full_pack") {
            OutlinedButton(
                onClick = onRegenerate,
                modifier = Modifier.fillMaxWidth(),
                enabled = regenerationRemaining > 0
            ) {
                Text(
                    if (regenerationRemaining > 0)
                        "Regenerate ($regenerationRemaining left)"
                    else
                        "Regenerations exhausted"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Confirm button
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedIndex != null
        ) {
            Text(
                if (purchasedProductId == "full_pack")
                    "Confirm Selection & Unlock HD"
                else
                    "Confirm Selection & Unlock HD ($6.99)"
            )
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String?,
    hasPurchase: Boolean,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = errorMessage ?: "Generation failed",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (hasPurchase) {
                Button(onClick = onRetry) {
                    Text("Try Again")
                }
            } else {
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
