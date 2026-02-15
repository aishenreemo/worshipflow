package com.example.workshipflow.presentation.songs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workshipflow.data.Song
import com.example.workshipflow.ui.WorshipViewModel
import com.example.workshipflow.utils.ChordTransposer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSongScreen(
    songId: Long,
    viewModel: WorshipViewModel,
    onBack: () -> Unit
) {
    val songFlow = remember(songId) { viewModel.getSongFlow(songId) }
    val song by songFlow.collectAsState(initial = null as Song?)

    var title by remember(song) { mutableStateOf(song?.title ?: "") }
    var artist by remember(song) { mutableStateOf(song?.artist ?: "") }
    var content by remember(song) { mutableStateOf(song?.content ?: "") }
    var originalKey by remember(song) { mutableStateOf(song?.originalKey ?: "C") }
    var currentKey by remember(song) { mutableStateOf(song?.currentKey ?: "C") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Song") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (song == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var originalKeyExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = originalKey,
                            onValueChange = { originalKey = it },
                            label = { Text("Original Key") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { originalKeyExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(expanded = originalKeyExpanded, onDismissRequest = { originalKeyExpanded = false }) {
                            ChordTransposer.keys.forEach { key ->
                                DropdownMenuItem(
                                    text = { Text(key) },
                                    onClick = {
                                        originalKey = key
                                        originalKeyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var currentKeyExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = currentKey,
                            onValueChange = { currentKey = it },
                            label = { Text("Current Key") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { currentKeyExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(expanded = currentKeyExpanded, onDismissRequest = { currentKeyExpanded = false }) {
                            ChordTransposer.keys.forEach { key ->
                                DropdownMenuItem(
                                    text = { Text(key) },
                                    onClick = {
                                        currentKey = key
                                        currentKeyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth().height(400.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        song?.let {
                            viewModel.updateSong(
                                it.copy(
                                    title = title,
                                    artist = artist,
                                    content = content,
                                    originalKey = originalKey,
                                    currentKey = currentKey
                                )
                            )
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}
