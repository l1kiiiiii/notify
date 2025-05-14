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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("All Scheduled Tasks") })
        }
    ) { innerPadding ->
        Column( // Use Column to arrange search bar and list vertically
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    taskViewModel.updateSearchQuery(newValue) // Update search query in ViewModel
                },
                label = { Text("Search tasks") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            if (allTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth() // Ensure Box takes full width within the Column
                        .weight(1f), // Let the Box take remaining space
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks scheduled yet or matching search.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f) // Let LazyColumn take remaining space
                ) {
                    items(allTasks) { task ->
                        TaskItem(
                            task = task,
                            onDeleteClick = { taskToDelete ->
                                taskViewModel.deleteTask(taskToDelete)
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onDeleteClick: (Task) -> Unit
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