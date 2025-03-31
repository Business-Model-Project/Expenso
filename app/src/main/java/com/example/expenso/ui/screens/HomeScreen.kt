package com.example.expenso.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expenso.viewmodel.ExpenseViewModel
import com.example.expenso.data.Expense
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController, expenseViewModel: ExpenseViewModel = viewModel()) {
    val expenses by expenseViewModel.expenses.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Expense Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))



        if (expenses.isEmpty()) {
            Text("No expenses found", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(expenses) { expense ->
                    ExpenseItem(expense)
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    val dateFormatted = expense.date?.toDate()?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
    } ?: "Unknown Date"  // ✅ If null, show "Unknown Date"

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = expense.category, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Amount: €${expense.amount}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Date: $dateFormatted", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
