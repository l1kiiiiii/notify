package com.example.notify.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.ui.viewmodel.TaskViewModel
import com.example.notify.ui.viewmodel.TaskViewModelFactory
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
    val taskState by taskViewModel.getTaskById(taskId).collectAsState(initial = null) // Rename the delegated property

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Call the lambda on back button click
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        taskState?.let { task -> // Introduce a local variable 'task'
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                Text("Title: ${task.title}")
                Text("Details: ${task.details}")
                Text("Scheduled for: ${SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(task.scheduledTimeMillis))}")
            }
        }
    }
}