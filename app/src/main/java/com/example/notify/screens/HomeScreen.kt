package com.example.notify.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.data.Task
import com.example.notify.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    // Get the ViewModel
    taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(LocalContext.current)) // Get the same ViewModel
) {
    // Observe the list of tasks from the ViewModel
    val allTasks by taskViewModel.allTasks.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("All Scheduled Tasks") })
        }
    ) { innerPadding ->
        if (allTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks scheduled yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(allTasks) { task ->
                    HomeScreenTaskItem(task = task)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun HomeScreenTaskItem(task: Task) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(task.scheduledTimeMillis))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = task.title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = task.details, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Scheduled for: $formattedTime", style = MaterialTheme.typography.bodySmall)
    }
}

