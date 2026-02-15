package com.example.workshipflow.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workshipflow.data.Setlist
import com.example.workshipflow.ui.WorshipViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WorshipViewModel,
    onLibraryClick: () -> Unit,
    onAddSongClick: () -> Unit,
    onAddServiceClick: () -> Unit,
    onSongClick: (Long) -> Unit,
    onUpcomingServiceClick: (Long) -> Unit
) {
    val recentSongs by viewModel.recentSongs.collectAsState(initial = emptyList())
    val currentSetlist by viewModel.currentSetlist.collectAsState(initial = null as Setlist?)
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("WorshipFlow", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                currentSetlist?.let { onUpcomingServiceClick(it.id) }
            },
            enabled = currentSetlist != null,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Upcoming Service", style = MaterialTheme.typography.titleMedium)
                if (currentSetlist != null) {
                    Text(currentSetlist!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(dateFormat.format(Date(currentSetlist!!.date)), style = MaterialTheme.typography.bodyLarge)
                    if (currentSetlist!!.leader.isNotBlank()) {
                        Text("Leader: ${currentSetlist!!.leader}", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text("No services scheduled.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Quick Actions", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardActionCard(Modifier.weight(1f), "Song Library", Icons.AutoMirrored.Filled.List, onLibraryClick)
            DashboardActionCard(Modifier.weight(1f), "Add Song", Icons.Default.Add, onAddSongClick)
            DashboardActionCard(Modifier.weight(1f), "Schedule", Icons.Default.DateRange, onAddServiceClick)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Recent Songs", style = MaterialTheme.typography.titleLarge)
        if (recentSongs.isEmpty()) {
            Text(
                "No recent songs. Start by adding some to your library!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            recentSongs.forEach { song ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSongClick(song.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    ListItem(
                        headlineContent = { Text(song.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(song.artist, style = MaterialTheme.typography.bodySmall) },
                        leadingContent = { Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardActionCard(modifier: Modifier, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title)
            Text(title, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
