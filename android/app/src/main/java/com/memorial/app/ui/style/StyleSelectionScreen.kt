package com.memorial.app.ui.style

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorial.app.data.model.PhotoStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleSelectionScreen(
    projectId: String,
    onStyleSelected: () -> Unit,
    onBack: () -> Unit,
    viewModel: StyleSelectionViewModel = viewModel()
) {
    val selectedStyle by viewModel.selectedStyle.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Style") },
                navigationIcon = {
                    OutlinedButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select the style for your memorial photo:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PhotoStyle.values().forEach { style ->
                StyleCard(
                    style = style,
                    selected = style == selectedStyle,
                    onSelect = { viewModel.selectStyle(style) }
                )
            }

            Button(
                onClick = onStyleSelected,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun StyleCard(
    style: PhotoStyle,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val (title, description) = when (style) {
        PhotoStyle.NATURAL_FAMILY -> "Natural Family Photo" to "A warm, candid family-style portrait"
        PhotoStyle.VINTAGE_RESTORE -> "Vintage Restore" to "A restored, timeless memory feel"
        PhotoStyle.BIRTHDAY -> "Birthday Memorial" to "Celebrate a birthday together again"
        PhotoStyle.GRADUATION_WEDDING_HOLIDAY -> "Special Occasion" to "Graduation, wedding, or holiday scene"
    }

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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
