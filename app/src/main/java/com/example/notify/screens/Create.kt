package com.example.notify.screens

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.data.DatabaseProvider
import com.example.notify.data.Task
import com.example.notify.ui.viewmodel.TaskViewModel
import java.util.Calendar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Create(
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDetails by remember { mutableStateOf("") }
    val categories = listOf("General", "Work", "Personal", "Shopping", "Study")
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    // FIX: Declare the missing state variable for the dropdown menu
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val coroutineScope = rememberCoroutineScope()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions", "POST_NOTIFICATIONS permission granted")
        } else {
            Log.d("Permissions", "POST_NOTIFICATIONS permission denied")
            Toast.makeText(context, "Notification permission denied. Notifications are needed for reminders.", Toast.LENGTH_LONG).show()
        }
    }

    // Function containing the core task scheduling logic
    fun scheduleTaskLogic() {
        Log.d("CreateScreen", "Executing scheduleTaskLogic")
        // FIX: Reorder the check logic to be more intuitive
        if (taskTitle.isNotBlank() && selectedDate != null && selectedTime != null) {
            val taskCalendar = Calendar.getInstance().apply {
                set(
                    selectedDate!!.get(Calendar.YEAR),
                    selectedDate!!.get(Calendar.MONTH),
                    selectedDate!!.get(Calendar.DAY_OF_MONTH)
                )
                set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
            }

            val task = Task(
                title = taskTitle,
                details = taskDetails,
                scheduledTimeMillis = taskCalendar.timeInMillis,
                category = selectedCategory
            )

            taskViewModel.insertTask(task)

            Toast.makeText(context, "Task scheduled!", Toast.LENGTH_SHORT).show()

            taskTitle = ""
            taskDetails = ""
            selectedCategory = categories[0]
            selectedDate = null
            selectedTime = null

            onNavigateBack()
        } else {
            Toast.makeText(context, "Please fill all fields and select a valid date/time", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to check permissions and then call scheduleTaskLogic
    fun scheduleTaskWithPermissionCheck() {
        Log.d("CreateScreen", "Checking permissions before scheduling")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // For POST_NOTIFICATIONS (API 33+)
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("CreateScreen", "POST_NOTIFICATIONS already granted. Checking exact alarm.")
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (alarmManager.canScheduleExactAlarms()) {
                        Log.d("CreateScreen", "canScheduleExactAlarms granted. Scheduling task.")
                        scheduleTaskLogic()
                    } else {
                        Log.d("CreateScreen", "Exact alarm permission needed. Redirecting to settings.")
                        Toast.makeText(context, "Please allow exact alarms for timely reminders in app settings.", Toast.LENGTH_LONG).show()
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    }
                }
                else -> {
                    Log.d("CreateScreen", "Requesting POST_NOTIFICATIONS permission.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("CreateScreen", "Pre-API 33. Checking exact alarm.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // For SCHEDULE_EXACT_ALARM (API 31+)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d("CreateScreen", "canScheduleExactAlarms granted (Pre-API 33). Scheduling task.")
                    scheduleTaskLogic()
                } else {
                    Log.d("CreateScreen", "Exact alarm permission needed (Pre-API 33). Redirecting to settings.")
                    Toast.makeText(context, "Please allow exact alarms for timely reminders in app settings.", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                }
            } else {
                Log.d("CreateScreen", "Pre-API 31 (and Pre-API 33). Scheduling task directly.")
                scheduleTaskLogic()
            }
        }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Schedule Task") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = taskDetails,
                onValueChange = { taskDetails = it },
                label = { Text("Task Details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryDropdown,
                    onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { datePickerDialog.show() }) {
                    Text(
                        selectedDate?.let {
                            "${it.get(Calendar.DAY_OF_MONTH)}/${it.get(Calendar.MONTH) + 1}/${it.get(Calendar.YEAR)}"
                        } ?: "Select Date"
                    )
                }

                Button(onClick = { timePickerDialog.show() }) {
                    Text(
                        selectedTime?.let {
                            "${it.get(Calendar.HOUR_OF_DAY)}:${it.get(Calendar.MINUTE).toString().padStart(2, '0')}"
                        } ?: "Select Time"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cancel Task",
                    modifier = Modifier
                        .clickable {
                            Log.d("CreateScreen", "Cancel button clicked")
                            taskTitle = ""
                            taskDetails = ""
                            // FIX: Change taskCategory to selectedCategory
                            selectedCategory = categories[0]
                            selectedDate = null
                            selectedTime = null
                            onNavigateBack()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = {
                        Log.d("CreateScreen", "SET button clicked")
                        scheduleTaskWithPermissionCheck()
                    },
                ) {
                    Text("Save Task")
                }
            }
        }
    }
}

class TaskViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(DatabaseProvider.getTaskDao(context), context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Preview
@Composable
fun CreatePreview() {
    Create(onNavigateBack = { /* Handle back navigation */ })
}
