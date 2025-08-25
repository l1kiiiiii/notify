package com.example.notify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notify.screens.Create
import com.example.notify.screens.HomeScreen
import com.example.notify.screens.TaskDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navItemList = listOf(
        Navitem("home", Icons.Default.Home),
    )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create") },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Create, contentDescription = "Add Task")
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
        bottomBar = {
            Box {
                NavigationBar(
                    modifier = Modifier,
                    containerColor = Color.Transparent,
                ) {
                    navItemList.forEach { navItem ->
                        NavigationBarItem(
                            selected = currentRoute == navItem.label,
                            onClick = {
                                navController.navigate(navItem.label) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("create") {
                Create(onNavigateBack = { navController.popBackStack() })
            }
            composable("taskdetails/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                if (taskId != null) {
                    TaskDetailsScreen(taskId = taskId, onNavigateBack = { navController.popBackStack() })
                } else {
                    navController.popBackStack()
                }
            }
        }
    }
}

data class Navitem(
    val label: String,
    val icon: ImageVector
)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}