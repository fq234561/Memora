package com.memorial.app.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
fun CreateProjectScreen(
    onProjectCreated: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: CreateProjectViewModel = viewModel()
) {
    val title by viewModel.title.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Memorial Project") })
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
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Project Title") },
                placeholder = { Text("e.g., For Mom") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedButton(
                onClick = { viewModel.onTitleChange("Test Memorial") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🧪 Fill Test Title")
            }

            Text(
                text = "Photo Style",
                style = MaterialTheme.typography.titleSmall
            )

            PhotoStyle.values().forEach { style ->
                StyleOption(
                    style = style,
                    selected = style == selectedStyle,
                    onSelect = { viewModel.onStyleSelected(style) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isCreating) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { viewModel.createProject(onProjectCreated) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank()
                ) {
                    Text("Create Project")
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun StyleOption(
    style: PhotoStyle,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val label = when (style) {
        PhotoStyle.NATURAL_FAMILY -> "Natural Family Photo"
        PhotoStyle.VINTAGE_RESTORE -> "Vintage Restore"
        PhotoStyle.BIRTHDAY -> "Birthday Memorial"
        PhotoStyle.GRADUATION_WEDDING_HOLIDAY -> "Graduation / Wedding / Holiday"
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
        Text(
            text = label,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
