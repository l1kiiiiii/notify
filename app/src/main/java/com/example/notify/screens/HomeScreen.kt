package com.example.notify.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.liveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.notify.MainScreen
import com.example.notify.data.Task
import com.example.notify.data.TaskStatus
import com.example.notify.ui.viewmodel.TaskViewModel
import com.example.notify.ui.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(LocalContext.current)
    )
) {
    DisposableEffect(navController) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val taskCreatedLiveData = savedStateHandle?.getLiveData<Boolean>("taskCreated")

        val observer = androidx.lifecycle.Observer<Boolean> { created ->
            if (created == true) {
                //taskViewModel.loadTasks //Refresh tasks
                savedStateHandle?.set("taskCreated", false) // Reset the flag
            }
        }

        taskCreatedLiveData?.observeForever(observer)

        // Ensure the observer is removed when the effect leaves the composition
        onDispose {
            taskCreatedLiveData?.removeObserver(observer)
        }
    }
    // Collect StateFlow as State for Compose
    val allTasks by taskViewModel.filteredTasks.collectAsState()
    val searchQuery by taskViewModel.searchQuery.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var editedTitle by remember { mutableStateOf("") }
    var editedCategory by remember { mutableStateOf("") }
    var editedDetails by remember { mutableStateOf("") }
    var editedScheduledTime by remember { mutableStateOf<Long?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = taskToEdit?.scheduledTimeMillis
    )

    // Reinitialize timePickerState when taskToEdit changes
    val timePickerState = rememberTimePickerState(
        initialHour = taskToEdit?.let {
            Calendar.getInstance().apply { timeInMillis = it.scheduledTimeMillis }
                .get(Calendar.HOUR_OF_DAY)
        } ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = taskToEdit?.let {
            Calendar.getInstance().apply { timeInMillis = it.scheduledTimeMillis }
                .get(Calendar.MINUTE)
        } ?: Calendar.getInstance().get(Calendar.MINUTE)
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Snackbar for user feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // topBar = { TopAppBar(title = { Text("Notify") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 4.dp)
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
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Task deleted")
                                }
                            },
                            onEditClick = { currentTaskToEdit ->
                                taskToEdit = currentTaskToEdit
                                editedTitle = currentTaskToEdit.title
                                editedDetails = currentTaskToEdit.details
                                editedCategory = currentTaskToEdit.category
                                editedScheduledTime = currentTaskToEdit.scheduledTimeMillis
                                showEditDialog = true
                            },
                            onStatusClick = { newStatus ->
                                taskViewModel.updateTaskStatus(task.id, newStatus)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Task status updated to ${newStatus.displayName}")
                                }
                            },
                            onTaskClick = { taskId ->
                                navController.navigate("taskdetails/$taskId")
                            }
                        )
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
                    OutlinedTextField(
                        value = editedCategory,
                        onValueChange = { editedCategory = it },
                        label = { Text("Category") },
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
                                category = if (editedCategory.isBlank()) "General" else editedCategory.trim(),
                                scheduledTimeMillis = editedScheduledTime ?: task.scheduledTimeMillis
                            )
                            taskViewModel.updateTask(updatedTask)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Task updated")
                            }
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

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            datePickerState.selectedDateMillis?.let { dateMillis ->
                                timeInMillis = dateMillis
                            }
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
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
    onStatusClick: (TaskStatus) -> Unit,
    onEditClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(task.scheduledTimeMillis))

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClick(task.id) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskProgressIndicator(
            status = task.status,
            onStatusClick = {
                val newStatus = when (task.status) {
                    TaskStatus.UPCOMING -> TaskStatus.ACTIVE
                    TaskStatus.ACTIVE -> TaskStatus.COMPLETED
                    TaskStatus.COMPLETED -> TaskStatus.ACTIVE
                }
                onStatusClick(newStatus)
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (task.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = task.details, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Category: ${task.category}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Scheduled for: $formattedTime", style = MaterialTheme.typography.bodySmall)
        }

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        expanded = false
                        onEditClick(task)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        expanded = false
                        onDeleteClick(task)
                    }
                )
            }
        }
    }
}

@Composable
fun TaskProgressIndicator(
    status: TaskStatus,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onStatusClick() }
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = when (status) {
                        TaskStatus.COMPLETED -> Color.Green
                        TaskStatus.ACTIVE -> Color.Blue
                        TaskStatus.UPCOMING -> Color.LightGray
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (status == TaskStatus.COMPLETED) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (status) {
                TaskStatus.COMPLETED -> "Completed"
                TaskStatus.UPCOMING -> "Upcoming"
                TaskStatus.ACTIVE -> "Active"
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
