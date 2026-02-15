package com.example.workshipflow.presentation.setlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import com.example.workshipflow.data.Setlist
import com.example.workshipflow.data.Song
import com.example.workshipflow.ui.WorshipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongToSetlistScreen(
    setlistId: Long,
    viewModel: WorshipViewModel,
    onBack: () -> Unit
) {
    val songs by viewModel.allSongs.collectAsState(initial = emptyList())
    val setlist by viewModel.getSetlistById(setlistId).collectAsState(initial = null as Setlist?)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Song to Setlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(songs) { song ->
                ListItem(
                    headlineContent = { Text(song.title) },
                    supportingContent = { Text(song.artist) },
                    modifier = Modifier.clickable {
                        setlist?.let { viewModel.addSongToSetlist(it, song.id) }
                        onBack()
                    }
                )
            }
        }
    }
}