package com.example.workshipflow.presentation.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workshipflow.ui.WorshipViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    viewModel: WorshipViewModel,
    onBack: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    var name by remember { mutableStateOf("Sunday Service") }
    var leader by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Service") },
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
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Service Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Date", style = MaterialTheme.typography.labelSmall)
                        Text(
                            datePickerState.selectedDateMillis?.let { formatter.format(Date(it)) } ?: "Select Date",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = leader,
                onValueChange = { leader = it },
                label = { Text("Worship Leader (Optional)") },
                placeholder = { Text("Optional") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        viewModel.addSetlist(
                            name = name,
                            date = datePickerState.selectedDateMillis ?: System.currentTimeMillis(),
                            leader = leader
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Text("Save Schedule")
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
