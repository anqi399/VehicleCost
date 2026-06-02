package com.example.vehiclecost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vehiclecost.ui.screen.DashboardScreen
import com.example.vehiclecost.ui.screen.HomeScreen
import com.example.vehiclecost.ui.screen.SettingsScreen
import com.example.vehiclecost.ui.viewmodel.CostViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.vehiclecost.ui.component.AddCostDialog

@Composable
fun MainApp(viewModel: CostViewModel) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "爱车") },
                    label = { Text("爱车") },
                    selected = currentRoute == "dashboard",
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "账单") },
                    label = { Text("账单") },
                    selected = currentRoute == "records",
                    onClick = {
                        navController.navigate("records") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                    label = { Text("设置") },
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == "records" || currentRoute == "dashboard") {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "记一笔")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = "dashboard") {
                composable("dashboard") { DashboardScreen(viewModel) }
                composable("records") { HomeScreen(viewModel) } // Reuse HomeScreen for Records
                composable("settings") { SettingsScreen(viewModel) }
            }
        }
    }

    if (showAddDialog) {
        // We need to pass the selectedMonth to initialize date correctly
        val selectedMonth by viewModel.selectedMonth.collectAsState()
        
        AddCostDialog(
            initialCost = null,
            selectedMonthPattern = selectedMonth,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, category, date, note, tag ->
                viewModel.addCost(amount, category, date, note, tag)
                showAddDialog = false
                showSuccessToast = true
                coroutineScope.launch {
                    delay(800)
                    showSuccessToast = false
                }
            }
        )
    }

    if (showSuccessToast) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = showSuccessToast,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("✅ 保存成功", color = Color.White, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
