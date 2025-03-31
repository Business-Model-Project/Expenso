package com.example.expenso.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expenso.ui.navigation.BottomNavBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Track current route
    var currentRoute by remember { mutableStateOf("") }

    // Observe route changes
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: ""
        }
    }

    // List of screens where the BottomBar should be hidden
    val hideBottomBarScreens = listOf("landing", "login", "signup")

    Scaffold(
        bottomBar = {
            if (currentRoute !in hideBottomBarScreens) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MainNavigationGraph(navController)
        }
    }
}

@Composable
fun MainNavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") { LandingScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("add_expense") {
            AddExpenseScreen(onExpenseAdded = { navController.popBackStack() })
        }
        composable("reports") { ReportsScreen() }
        composable("categories") { CategoriesScreen() }
        composable("settings") {
            SettingsScreen(onLogoutSuccess = {
                navController.navigate("landing") {
                    popUpTo(0) { inclusive = true } // Clears the entire back stack
                }
            })
        }
    }
}
