package com.example.workshipflow.presentation.songs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workshipflow.data.Song
import com.example.workshipflow.ui.WorshipViewModel
import com.example.workshipflow.utils.ChordTransposer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    songId: Long,
    viewModel: WorshipViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val songFlow = remember(songId) { viewModel.getSongFlow(songId) }
    val currentSong by songFlow.collectAsState(initial = null as Song?)
    
    val keys = ChordTransposer.keys
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    var isScrolling by remember { mutableStateOf(false) }
    var scrollSpeed by remember { mutableFloatStateOf(1f) }
    var showControls by remember { mutableStateOf(true) }
    var preferFlats by remember { mutableStateOf(false) }

    LaunchedEffect(isScrolling, scrollSpeed) {
        if (isScrolling) {
            while (isActive) {
                scrollState.scrollBy(scrollSpeed)
                delay(16) // ~60fps
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentSong?.title ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(songId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Song")
                    }
                    IconButton(onClick = {
                        currentSong?.let {
                            viewModel.deleteSong(it)
                            Toast.makeText(context, "Song deleted", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        currentSong?.let { song ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Key: ", fontWeight = FontWeight.Bold)
                        
                        IconButton(onClick = {
                            val nextKey = ChordTransposer.getNextKey(song.currentKey, -1, preferFlats)
                            viewModel.updateSong(song.copy(currentKey = nextKey))
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Transpose Down")
                        }

                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(song.currentKey, style = MaterialTheme.typography.titleMedium)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                keys.forEach { key ->
                                    DropdownMenuItem(
                                        text = { Text(key) },
                                        onClick = {
                                            viewModel.updateSong(song.copy(currentKey = key))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = {
                            val nextKey = ChordTransposer.getNextKey(song.currentKey, 1, preferFlats)
                            viewModel.updateSong(song.copy(currentKey = nextKey))
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Transpose Up")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("b/#", style = MaterialTheme.typography.labelMedium)
                        Switch(
                            checked = preferFlats,
                            onCheckedChange = { 
                                preferFlats = it 
                                val normalizedKey = ChordTransposer.shiftChord(song.currentKey, 0, it)
                                if (normalizedKey != song.currentKey) {
                                    viewModel.updateSong(song.copy(currentKey = normalizedKey))
                                }
                            },
                            modifier = Modifier.scale(0.8f)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        val semitones = ChordTransposer.getSemitonesBetween(song.originalKey, song.currentKey)
                        if (semitones != 0) {
                            val capo = (12 - semitones) % 12
                            if (capo != 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "Capo $capo",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val transposedContent = ChordTransposer.transposeContent(
                        song.content,
                        song.originalKey,
                        song.currentKey,
                        preferFlats
                    )

                    Box(modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                showControls = !showControls
                            }
                        }
                    ) {
                        ChordProViewer(transposedContent, scrollState)
                    }
                }

                // Teleprompter Controls Overlay
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).width(280.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isScrolling = !isScrolling }) {
                                    Icon(
                                        if (isScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isScrolling) "Pause" else "Play"
                                    )
                                }
                                Text("Speed", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = scrollSpeed,
                                    onValueChange = { scrollSpeed = it },
                                    valueRange = 0.1f..5f,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                )
                                Text(String.format("%.1fx", scrollSpeed), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChordProViewer(content: String, scrollState: LazyListState = rememberLazyListState()) {
    val lines = content.split("\n")
    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
        items(lines) { line ->
            ChordLine(line)
        }
    }
}

@Composable
fun ChordLine(line: String) {
    val chordPattern = "\\[([^]]+)]".toRegex()
    val matches = chordPattern.findAll(line).toList()

    if (matches.isEmpty()) {
        Text(line, modifier = Modifier.padding(vertical = 4.dp), style = MaterialTheme.typography.bodyLarge)
        return
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            matches.forEach { match ->
                val chord = match.groupValues[1]
                val offset = (match.range.first * 8).dp
                Text(
                    text = chord,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = offset)
                )
            }
        }
        Text(line.replace(chordPattern, " "), style = MaterialTheme.typography.bodyLarge)
    }
}
