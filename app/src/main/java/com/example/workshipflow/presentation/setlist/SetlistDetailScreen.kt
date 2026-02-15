package com.example.workshipflow.presentation.setlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workshipflow.data.Setlist
import com.example.workshipflow.data.Song
import com.example.workshipflow.ui.WorshipViewModel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistDetailScreen(
    setlistId: Long,
    viewModel: WorshipViewModel,
    onBack: () -> Unit,
    onAddSong: (Long) -> Unit,
    onSongClick: (Long) -> Unit,
    onEditSetlist: (Long) -> Unit
) {
    val setlist by viewModel.getSetlistById(setlistId).collectAsState(initial = null as Setlist?)
    val songs by viewModel.getSongsByIds(setlist?.songIds ?: emptyList()).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(setlist?.name ?: "Setlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditSetlist(setlistId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Setlist")
                    }
                    IconButton(onClick = {
                        setlist?.let { viewModel.deleteSetlist(it) }
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Setlist")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    SongListItem(song = song, onMoveUp = {
                        if (index > 0) {
                            setlist?.let { viewModel.reorderSongInSetlist(it, index, index - 1) }
                        }
                    }, onMoveDown = {
                        if (index < songs.size - 1) {
                            setlist?.let { viewModel.reorderSongInSetlist(it, index, index + 1) }
                        }
                    }, onRemove = {
                        setlist?.let { viewModel.removeSongFromSetlist(it, song.id) }
                    }, onClick = {
                        onSongClick(song.id)
                    })
                }
            }
            Button(
                onClick = { onAddSong(setlistId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Song")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Song")
            }
        }
    }
}

@Composable
private fun SongListItem(
    song: Song,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(song.title) },
        supportingContent = { Text(song.artist) },
        modifier = Modifier.clickable(onClick = onClick),
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up")
                }
                IconButton(onClick = onMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
        }
    )
}