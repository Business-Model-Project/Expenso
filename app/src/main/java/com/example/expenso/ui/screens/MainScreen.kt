package com.example.expenso.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expenso.ui.navigation.BottomNavBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) } //  bottom bar is always visible
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) //  Avoids content overlapping the bottom nav bar
        ) {
            MainNavigationGraph(navController)
        }
    }
}

@Composable
fun MainNavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("add_expense") { AddExpenseScreen() }
        composable("reports") { ReportsScreen() }
        composable("categories") { CategoriesScreen() }
        composable("settings") { SettingsScreen() }
    }
}
