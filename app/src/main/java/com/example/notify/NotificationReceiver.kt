package com.example.notify // Replace with your package name

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e("NotificationReceiver", "Context or Intent is null.")
            return
        }

        // Retrieve data from intent extras
        val taskId = intent.getLongExtra("task_id", -1L) // Use the same key as in NotificationScheduler
        val title = intent.getStringExtra("title") // Use the same key as in NotificationScheduler
        val message = intent.getStringExtra("message") // Use the same key as in NotificationScheduler

        if (taskId == -1L || title == null || message == null) {
            Log.e("NotificationReceiver", "Received intent with missing task data.")
            return
        }

        Log.d("NotificationReceiver", "Received broadcast for task ID: $taskId")

        // You might want to get the actual Task object from the database here
        // using the taskId if you need more details than what's in the intent extras.
        // This would require injecting your TaskDao or getting it via a dependency locator.
        // For simplicity here, we'll just use the data from the intent.

        // Create and show the notification
        // Use the task ID (converted to Int) as the unique notification ID
        val notificationId = taskId.toInt() // Be aware of potential Int overflow for very large IDs
        showNotification(context, notificationId, title, message)
    }

    private fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        // Check for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33 is TIRAMISU
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, log a warning and do not show the notification
                Log.w("NotificationReceiver", "POST_NOTIFICATIONS permission not granted. Cannot show notification for task ID: $notificationId")
                return
            }
        }

        // Permission is granted (or not required on older Android versions), proceed to show the notification
        val channelId = NotificationScheduler.CHANNEL_ID // Get the channel ID from your NotificationScheduler object

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon resource
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Remove the notification when the user taps it
        // Consider adding an Intent to launch your app when the notification is tapped
        // .setContentIntent(pendingIntentForAppLaunch)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
        Log.d("NotificationReceiver", "Notification shown for task ID (notification ID): $notificationId")
    }

    // createNotificationChannel belongs in NotificationScheduler, not here.
    // fun createNotificationChannel(context: Context) { ... }
}