package com.example.workshipflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workshipflow.data.Song
import com.example.workshipflow.ui.WorshipViewModel
import com.example.workshipflow.ui.theme.WorkshipFlowTheme
import com.example.workshipflow.utils.ChordTransposer
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkshipFlowTheme {
                WorshipApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorshipApp() {
    val navController = rememberNavController()
    val viewModel: WorshipViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "songs" || currentRoute == "calendar") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Songs") },
                        label = { Text("Songs") },
                        selected = currentRoute == "songs",
                        onClick = { if (currentRoute != "songs") navController.navigate("songs") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                        label = { Text("Calendar") },
                        selected = currentRoute == "calendar",
                        onClick = { if (currentRoute != "calendar") navController.navigate("calendar") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "songs",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("songs") {
                SongListScreen(
                    viewModel = viewModel,
                    onSongClick = { id -> navController.navigate("songDetail/$id") },
                    onAddClick = { navController.navigate("addSong") }
                )
            }
            composable(
                "songDetail/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.LongType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getLong("songId") ?: return@composable
                SongDetailScreen(
                    songId = songId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("addSong") {
                AddSongScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    viewModel = viewModel,
                    onAddClick = {
                        val name = "Service ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())}"
                        viewModel.addSetlist(name, System.currentTimeMillis())
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    viewModel: WorshipViewModel,
    onSongClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    val songs by viewModel.allSongs.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("WorshipFlow Library") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Song")
            }
        }
    ) { padding ->
        if (songs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your library is empty.", style = MaterialTheme.typography.bodyLarge)
                    Text("Tap + to add your first chart!", style = MaterialTheme.typography.bodySmall)
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: WorshipViewModel,
    onAddClick: () -> Unit
) {
    val setlists by viewModel.allSetlists.collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Worship Calendar") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
            }
        }
    ) { padding ->
        if (setlists.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No services scheduled.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(setlists) { setlist ->
                    ListItem(
                        headlineContent = { Text(setlist.name) },
                        supportingContent = { Text(dateFormat.format(Date(setlist.date))) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    songId: Long,
    viewModel: WorshipViewModel,
    onBack: () -> Unit
) {
    var songState by remember { mutableStateOf<Song?>(null) }
    val keys = listOf("C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B")

    LaunchedEffect(songId) {
        songState = viewModel.getSongById(songId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(songState?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        songState?.let { currentSong ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Transpose Key: ", fontWeight = FontWeight.Bold)
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(currentSong.currentKey, style = MaterialTheme.typography.titleMedium)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            keys.forEach { key ->
                                DropdownMenuItem(
                                    text = { Text(key) },
                                    onClick = {
                                        val updatedSong = currentSong.copy(currentKey = key)
                                        viewModel.updateSong(updatedSong)
                                        songState = updatedSong
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Capo calculation logic
                    val semitones = getSemitonesBetween(currentSong.originalKey, currentSong.currentKey)
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
                    currentSong.content,
                    currentSong.originalKey,
                    currentSong.currentKey
                )

                ChordProViewer(transposedContent)
            }
        }
    }
}

fun getSemitonesBetween(from: String, to: String): Int {
    val sharps = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val flats = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")
    val fromIndex = sharps.indexOf(from).let { if (it == -1) flats.indexOf(from) else it }
    val toIndex = sharps.indexOf(to).let { if (it == -1) flats.indexOf(to) else it }
    if (fromIndex == -1 || toIndex == -1) return 0
    return (toIndex - fromIndex + 12) % 12
}

@Composable
fun ChordProViewer(content: String) {
    val lines = content.split("\n")
    LazyColumn(modifier = Modifier.fillMaxSize()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongScreen(
    viewModel: WorshipViewModel,
    onBack: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("G") }

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
            Text("Link-to-Chart Automation", style = MaterialTheme.typography.titleMedium)
            Text("Import lyrics and chords instantly from a web URL.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Paste URL (e.g. worshipchart.com)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { 
                    viewModel.extractFromUrl(url)
                    onBack()
                },
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
            ) {
                Text("Automate Extraction")
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
                label = { Text("Original Key") },
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
                        viewModel.addSong(title, key, content)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Save to Library")
            }
        }
    }
}
