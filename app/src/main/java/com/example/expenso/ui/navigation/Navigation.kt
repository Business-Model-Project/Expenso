package com.example.expenso.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expenso.ui.screens.LoginScreen
import com.example.expenso.ui.screens.SignupScreen
import com.example.expenso.ui.screens.HomeScreen  // ✅ Ensure HomeScreen is imported

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignupScreen(navController)
        }
        composable("home") {  // ✅ Ensure "home" route exists
            HomeScreen(navController)
        }
    }
}
