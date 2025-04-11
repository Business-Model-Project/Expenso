package com.example.expenso.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.expenso.ui.navigation.BottomNavBar
import com.example.expenso.viewmodel.CategoryViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val categoryViewModel: CategoryViewModel = viewModel()

    var currentRoute by remember { mutableStateOf("") }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: ""
        }
    }

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
            MainNavigationGraph(navController, categoryViewModel)
        }
    }
}

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    categoryViewModel: CategoryViewModel
) {
    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") { LandingScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("add_expense") {
            AddExpenseScreen(onExpenseAdded = { navController.popBackStack() })
        }
        composable("edit_expense/{expenseId}") { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            EditExpenseScreen(navController, expenseId)
        }

        composable("reports") { ReportsScreen() }

        composable("categories") {
            CategoriesScreen(navController, categoryViewModel)
        }

        composable("add_category") {
            AddCategoryScreen(navController, categoryViewModel)
        }

        composable("settings") {
            SettingsScreen(onLogoutSuccess = {
                navController.navigate("landing") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        // ✅ Updated route to match EditCategoryScreen’s required parameters
        composable(
            route = "edit_category/{categoryId}/{categoryName}/{categoryDescription}/{encodedImageUrl}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType },
                navArgument("categoryDescription") { type = NavType.StringType },
                navArgument("encodedImageUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val categoryDescription = backStackEntry.arguments?.getString("categoryDescription") ?: ""
            val encodedImageUrl = backStackEntry.arguments?.getString("encodedImageUrl") ?: ""

            EditCategoryScreen(
                navController = navController,
                categoryId = categoryId,
                currentName = categoryName,
                currentDescription = categoryDescription,
                encodedImageUrl = encodedImageUrl,
                categoryViewModel = categoryViewModel
            )
        }
    }
}
