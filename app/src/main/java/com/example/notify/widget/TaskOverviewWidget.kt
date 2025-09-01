package com.example.notify.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.notify.data.TaskRepository

class TaskOverviewWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = androidx.glance.currentState<androidx.datastore.preferences.core.Preferences>()
            val tasks = prefs[TaskWidgetKeys.upcomingTasks].orEmpty()
            TaskOverviewContent(tasks)
        }
    }

    @Composable
    private fun TaskOverviewContent(tasks: Set<String>) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .padding(16.dp)
                .background(Color.DarkGray),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text("Upcoming Tasks")
            if (tasks.isEmpty()) {
                Text("No upcoming tasks 🎉")
            } else {
                tasks.take(3).forEach { task ->
                    Text("• $task")
                }
            }
            Button(
                text = "Refresh",
                onClick = actionRunCallback<RefreshAction>()
            )
        }
    }
}

object RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Get instance of TaskRepository using its getInstance method
        val repo = TaskRepository.getInstance(context.applicationContext) // Corrected instantiation
        val upcomingRaw = repo.getUpcomingTasks(
            System.currentTimeMillis(),
            limit = 3
        )

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[TaskWidgetKeys.upcomingTasks] = upcomingRaw.toSet()
        }

        TaskOverviewWidget().update(context, glanceId)
    }
}
