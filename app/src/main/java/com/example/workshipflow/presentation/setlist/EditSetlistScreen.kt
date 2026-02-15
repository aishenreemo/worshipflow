package com.example.workshipflow.presentation.setlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workshipflow.data.Setlist
import com.example.workshipflow.ui.WorshipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSetlistScreen(
    setlistId: Long,
    viewModel: WorshipViewModel,
    onBack: () -> Unit
) {
    val setlist by viewModel.getSetlistById(setlistId).collectAsState(initial = null as Setlist?)
    var name by remember(setlist) { mutableStateOf(setlist?.name ?: "") }
    var leader by remember(setlist) { mutableStateOf(setlist?.leader ?: "") }
    var theme by remember(setlist) { mutableStateOf(setlist?.theme ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Setlist") },
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
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Setlist Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = leader,
                onValueChange = { leader = it },
                label = { Text("Leader") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = theme,
                onValueChange = { theme = it },
                label = { Text("Theme") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    setlist?.let {
                        viewModel.updateSetlist(
                            it.copy(
                                name = name,
                                leader = leader,
                                theme = theme
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Save")
            }
        }
    }
}
