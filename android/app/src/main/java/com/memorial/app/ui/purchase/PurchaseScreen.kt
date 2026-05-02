package com.memorial.app.ui.purchase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    projectId: String,
    onPurchaseComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: PurchaseViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PurchaseViewModel(projectId) as T
            }
        }
    )
) {
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val isPurchasing by viewModel.isPurchasing.collectAsState()
    val purchaseComplete by viewModel.purchaseComplete.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(purchaseComplete) {
        if (purchaseComplete) {
            onPurchaseComplete()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unlock") },
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
                text = "Choose your package:",
                style = MaterialTheme.typography.titleMedium
            )

            PurchaseOptionCard(
                option = PurchaseViewModel.ProductOption.HD_PHOTO,
                selected = selectedProduct == PurchaseViewModel.ProductOption.HD_PHOTO,
                onSelect = { viewModel.selectProduct(PurchaseViewModel.ProductOption.HD_PHOTO) }
            )

            PurchaseOptionCard(
                option = PurchaseViewModel.ProductOption.PHOTO_AND_VIDEO,
                selected = selectedProduct == PurchaseViewModel.ProductOption.PHOTO_AND_VIDEO,
                onSelect = { viewModel.selectProduct(PurchaseViewModel.ProductOption.PHOTO_AND_VIDEO) }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isPurchasing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { viewModel.purchase() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedProduct != null
                ) {
                    Text("Purchase")
                }
            }
        }
    }
}

@Composable
private fun PurchaseOptionCard(
    option: PurchaseViewModel.ProductOption,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val containerColor = if (selected) {
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
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = option.price,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
