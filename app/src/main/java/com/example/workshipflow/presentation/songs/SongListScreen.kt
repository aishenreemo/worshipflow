package com.example.workshipflow.presentation.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.workshipflow.ui.WorshipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    viewModel: WorshipViewModel,
    onSongClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    val songs by viewModel.allSongs.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Song Library") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Song")
            }
        }
    ) { padding ->
        if (songs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No songs found. Tap + to add your first chart!")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(songs) { song ->
                    ListItem(
                        headlineContent = { Text(song.title, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Key: ${song.currentKey}") },
                        trailingContent = { Text(song.originalKey, color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.clickable { onSongClick(song.id) }
                    )
                }
            }
        }
    }
}
