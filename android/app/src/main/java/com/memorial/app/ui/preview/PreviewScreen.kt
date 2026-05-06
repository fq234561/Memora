package com.memorial.app.ui.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.memorial.app.ui.theme.BackgroundWarm
import com.memorial.app.ui.theme.DividerLight
import com.memorial.app.ui.theme.PrimaryGreen
import com.memorial.app.ui.theme.PrimaryGreenDark
import com.memorial.app.ui.theme.PrimaryGreenLight
import com.memorial.app.ui.theme.TextMuted
import com.memorial.app.ui.theme.TextPrimary
import com.memorial.app.ui.theme.TextSecondary

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
    val shouldNavigateToPurchase by viewModel.onNavigateToPurchase.collectAsState()
    val shouldNavigateToDownload by viewModel.onNavigateToDownload.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shouldNavigateToPurchase) {
        if (shouldNavigateToPurchase) {
            onProceedToPurchase()
            viewModel.onNavigateToPurchaseHandled()
        }
    }

    LaunchedEffect(shouldNavigateToDownload) {
        if (shouldNavigateToDownload) {
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

    var showRegenerateDialog by remember { mutableStateOf(false) }
    var adjustmentText by remember { mutableStateOf("") }
    var useAdjustment by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preview & Select",
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

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState) {
                is PreviewViewModel.PreviewUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
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
                        Text("Use Same Settings")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.RadioButton(
                            selected = useAdjustment,
                            onClick = { useAdjustment = true }
                        )
                        Text("Add Adjustment Note")
                    }
                    if (useAdjustment) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adjustmentText,
                            onValueChange = { adjustmentText = it },
                            label = { Text("Describe your desired adjustments") },
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

@Composable
private fun PurchaseRequiredContent(
    onPurchase: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ready to Generate Family Photo",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "After purchasing a preview pack, AI will generate 4 candidate family photos",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onPurchase,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(
                "Buy Preview Pack ($2.99)",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onPurchase,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Buy Full Pack ($12.99)")
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
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ready",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Photos and style preferences are ready. Now generating 4 AI candidate family photos.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(
                "Generate 4 Candidates",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
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
            CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Generating your family memory candidates...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            progress?.let {
                Text(
                    "$it%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { it / 100f },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryGreen,
                    trackColor = DividerLight
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
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Pick the one closest to what you want",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Text(
            text = "Tap to select a candidate image",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

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
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) PrimaryGreen else DividerLight,
                            shape = RoundedCornerShape(12.dp)
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
                                tint = PrimaryGreen,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(28.dp)
                            )
                        }
                    }
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

        if (purchasedProductId == "full_pack") {
            OutlinedButton(
                onClick = onRegenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = regenerationRemaining > 0
            ) {
                Text(
                    if (regenerationRemaining > 0)
                        "Regenerate ($regenerationRemaining remaining)"
                    else
                        "No regenerations left"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = selectedIndex != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                disabledContainerColor = DividerLight
            )
        ) {
            Text(
                if (purchasedProductId == "full_pack")
                    "Confirm & Unlock HD"
                else
                    "Confirm & Unlock HD ($6.99)",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
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
                text = errorMessage ?: "Generation Failed",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(if (hasPurchase) "Retry" else "Retry")
            }
        }
    }
}
