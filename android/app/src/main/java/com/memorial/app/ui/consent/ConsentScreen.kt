package com.memorial.app.ui.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentScreen(
    projectId: String,
    onConsentGiven: () -> Unit,
    onBack: () -> Unit,
    viewModel: ConsentViewModel = viewModel()
) {
    val hasRight by viewModel.hasRightToPhotos.collectAsState()
    val privateUse by viewModel.privateUseOnly.collectAsState()
    val understandAi by viewModel.understandAiGenerated.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consent") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Before we generate your memorial photo, please confirm:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConsentItem(
                checked = hasRight,
                onCheckedChange = { viewModel.toggleHasRightToPhotos() },
                text = "I have the right to use these photos."
            )

            ConsentItem(
                checked = privateUse,
                onCheckedChange = { viewModel.togglePrivateUseOnly() },
                text = "This is for private memorial use only."
            )

            ConsentItem(
                checked = understandAi,
                onCheckedChange = { viewModel.toggleUnderstandAiGenerated() },
                text = "I understand the result is AI-generated and not a real photograph."
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onConsentGiven,
                modifier = Modifier.fillMaxWidth(),
                enabled = hasRight && privateUse && understandAi
            ) {
                Text("I Agree & Continue")
            }
        }
    }
}

@Composable
private fun ConsentItem(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange() }
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
