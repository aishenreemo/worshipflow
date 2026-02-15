package com.example.workshipflow.presentation.songs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workshipflow.ui.WorshipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongScreen(
    viewModel: WorshipViewModel,
    onBack: () -> Unit,
    onBrowserClick: (String?) -> Unit,
    onSaved: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Song") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Search & Import", style = MaterialTheme.typography.titleMedium)
            Text("Search for chords on the web and extract them instantly.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Song or Artist Name") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { onBrowserClick(searchQuery) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )

            Button(
                onClick = { onBrowserClick(searchQuery) },
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                enabled = searchQuery.isNotBlank()
            ) {
                Icon(Icons.Default.Language, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Search with DuckDuckGo")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Manual Entry", style = MaterialTheme.typography.titleMedium)
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Song Title") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            TextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Original Key (Optional - will detect if empty)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Chart Content (ChordPro: [G]Amazing [C]Grace)") },
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 4.dp)
            )
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        viewModel.addSong(title, content, key.ifBlank { null }, onSaved = onSaved)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Save to Library")
            }
        }
    }
}
