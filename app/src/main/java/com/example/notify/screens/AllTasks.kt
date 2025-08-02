package com.example.notify.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.data.Task
import com.example.notify.ui.viewmodel.TaskViewModel
import com.example.notify.ui.viewmodel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasks(
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(LocalContext.current)
    )
) {
    // Observe filtered tasks from the ViewModel
    val allTasks by taskViewModel.filteredTasks.collectAsState(initial = emptyList())

    // Observe the search query from the ViewModel
    val searchQuery by taskViewModel.searchQuery.collectAsState()

    // State for edit dialog
    var showEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var editedTitle by remember { mutableStateOf("") }
    var editedDetails by remember { mutableStateOf("") }
    var editedScheduledTime by remember { mutableStateOf<Long?>(null) }

    // Date and Time picker states
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = taskToEdit?.scheduledTimeMillis // Re-keys when taskToEdit changes
        // or provide a default if taskToEdit is null initially
    )

    val timePickerState = rememberTimePickerState(
        // Initialize based on taskToEdit as well if needed, or default to current time
        initialHour = taskToEdit?.let { java.util.Calendar.getInstance().apply { timeInMillis = it.scheduledTimeMillis }.get(java.util.Calendar.HOUR_OF_DAY) } ?: java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
        initialMinute = taskToEdit?.let { java.util.Calendar.getInstance().apply { timeInMillis = it.scheduledTimeMillis }.get(java.util.Calendar.MINUTE) } ?: java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("All Scheduled Tasks") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    taskViewModel.updateSearchQuery(newValue)
                },
                label = { Text("Search tasks") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            if (allTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks scheduled yet or matching search.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(allTasks) { task ->
                        TaskItem(
                            task = task,
                            onDeleteClick = { taskToDelete ->
                                taskViewModel.deleteTask(taskToDelete)
                            },

                            onEditClick = { currentTaskToEdit ->
                                taskToEdit = currentTaskToEdit // This will trigger recomposition, re-remembering datePickerState with new initial value
                                editedTitle = currentTaskToEdit.title
                                editedDetails = currentTaskToEdit.details
                                editedScheduledTime = currentTaskToEdit.scheduledTimeMillis // Keep this for your formattedTime display and final save

                                // Update timePickerState directly since it has public setters
                                val calendar = java.util.Calendar.getInstance().apply {
                                    timeInMillis = currentTaskToEdit.scheduledTimeMillis
                                }
                                // These setters are available if using the material3 time picker state
                                // If rememberTimePickerState is from a different library, check its API
                                // For androidx.compose.material3.rememberTimePickerState, hour and minute are mutable properties
                                timePickerState.hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                                timePickerState.minute = calendar.get(java.util.Calendar.MINUTE)

                                showEditDialog = true
                            }

                        )
                        Divider()
                    }
                }
            }
        }
    }

    // Edit Task Dialog
    if (showEditDialog && taskToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Task") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedDetails,
                        onValueChange = { editedDetails = it },
                        label = { Text("Task Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val formattedTime = editedScheduledTime?.let {
                        SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(it))
                    } ?: "Select Date & Time"
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(formattedTime)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        taskToEdit?.let { task ->
                            val updatedTask = task.copy(
                                title = editedTitle.trim(),
                                details = editedDetails.trim(),
                                scheduledTimeMillis = editedScheduledTime ?: task.scheduledTimeMillis
                            )
                            taskViewModel.updateTask(updatedTask)
                        }
                        showEditDialog = false
                        taskToEdit = null
                    },
                    enabled = editedTitle.trim().isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) {
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

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = java.util.Calendar.getInstance().apply {
                            datePickerState.selectedDateMillis?.let { dateMillis ->
                                timeInMillis = dateMillis
                            }
                            set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(java.util.Calendar.MINUTE, timePickerState.minute)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        editedScheduledTime = calendar.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { content() },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
fun TaskItem(
    task: Task,
    onDeleteClick: (Task) -> Unit,
    onEditClick: (Task) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(task.scheduledTimeMillis))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = task.details, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Scheduled for: $formattedTime", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { onEditClick(task) }) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Task")
        }
        IconButton(onClick = { onDeleteClick(task) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete Task")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllTasksPreview() {
    AllTasks()
}