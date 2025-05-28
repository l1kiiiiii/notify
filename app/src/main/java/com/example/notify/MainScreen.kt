package com.example.notify

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.notify.screens.AllTasks
import com.example.notify.screens.Create
import com.example.notify.screens.HomeScreen
import com.example.notify.screens.TaskDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController() // Get the NavController

    val navItemList = listOf(
        Navitem("home", Icons.Default.Home), // Use routes as labels for easier comparison
        Navitem("create", Icons.Default.Create),
        Navitem("alltasks", Icons.Default.Star)
    )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEach { navItem ->
                    NavigationBarItem(
                        selected = currentRoute == navItem.label, // Compare with currentRoute
                        onClick = {
                            // Navigate using navController
                            navController.navigate(navItem.label, navOptions {
                                // Pop up to the start destination of the graph to avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            })
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.label,
                            )
                        },
                        label = {
                            Text(text = navItem.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Use NavHost here instead of ContentScreen
        NavHost(
            navController = navController,
            startDestination = "home", // Set your start destination
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToAddTask = { navController.navigate("create") },
                    onNavigateToAllTasks = { navController.navigate("alltasks") },
                    onNavigateToTaskDetails = { taskId -> navController.navigate("taskdetails/$taskId") }
                )
            }
            composable("create") {
                Create()
            }
            composable("alltasks") {
                AllTasks()
            }
            // Define the TaskDetails destination with the argument
            composable("taskdetails/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                if (taskId != null) {
                    TaskDetailsScreen(taskId = taskId, onNavigateBack = { navController.popBackStack() })
                } else {
                    // Handle the case where taskId is null (e.g., show an error or navigate back)
                    navController.popBackStack()
                }
            }
        }
    }
}

data class Navitem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}