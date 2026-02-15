package com.example.workshipflow.presentation.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.workshipflow.ui.WorshipViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: WorshipViewModel,
    onAddClick: () -> Unit,
    onSetlistClick: (Long) -> Unit
) {
    val setlists by viewModel.allSetlists.collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Worship Schedule") }) },
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
                    val isPast = setlist.date < System.currentTimeMillis()
                    ListItem(
                        headlineContent = { Text(setlist.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text(dateFormat.format(Date(setlist.date))) },
                        overlineContent = { if (setlist.leader.isNotBlank()) Text("Leader: ${setlist.leader}") },
                        modifier = Modifier.clickable { onSetlistClick(setlist.id) },
                        colors = if (isPast) {
                            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        } else {
                            ListItemDefaults.colors()
                        }
                    )
                }
            }
        }
    }
}
