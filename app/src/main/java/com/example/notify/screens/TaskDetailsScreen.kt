package com.example.notify.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Updated import
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.ui.viewmodel.TaskViewModel
import com.example.notify.ui.viewmodel.TaskViewModelFactory
import com.example.notify.data.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: Long,
    onNavigateBack: () -> Unit, // Lambda to trigger back navigation
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(LocalContext.current)
    )
) {
    val taskState by taskViewModel.getTaskById(taskId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Call the lambda on back button click
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") // Updated icon
                    }
                }
            )
        }
    ) { innerPadding ->
        taskState?.let { task ->
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                Text("Title: ${task.title}")
                Text("Details: ${task.details}")
                val scheduleText = task.scheduledTimeMillis?.let {
                    "Scheduled for: ${SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(it))}"
                } ?: "Not scheduled"
                Text(scheduleText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TaskDetailsScreenPreview() {
    // For preview purposes, creating a mock TaskViewModel or providing a sample task is fine.
    // Here we simulate a task being passed.
    val sampleTaskWithTime = Task(
        id = 1L,
        title = "Sample Task with Time",
        details = "This is a sample task description.",
        scheduledTimeMillis = System.currentTimeMillis() + 3600000 // 1 hour from now
    )
    val sampleTaskWithoutTime = Task(
        id = 2L,
        title = "Sample Task without Time",
        details = "This task has no specific schedule.",
        scheduledTimeMillis = null
    )

    // In a real scenario, the ViewModel would provide the task.
    // For preview, we can directly render the Column content or mock the ViewModel state.
    // For simplicity in preview, we'll just show one variant.
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Title: ${sampleTaskWithTime.title}")
        Text("Details: ${sampleTaskWithTime.details}")
        val scheduleTextWithTime = sampleTaskWithTime.scheduledTimeMillis?.let {
            "Scheduled for: ${SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(it))}"
        } ?: "Not scheduled"
        Text(scheduleTextWithTime)

        Text("\nTitle: ${sampleTaskWithoutTime.title}")
        Text("Details: ${sampleTaskWithoutTime.details}")
        val scheduleTextWithoutTime = sampleTaskWithoutTime.scheduledTimeMillis?.let {
            "Scheduled for: ${SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(it))}"
        } ?: "Not scheduled"
        Text(scheduleTextWithoutTime)
    }
}
