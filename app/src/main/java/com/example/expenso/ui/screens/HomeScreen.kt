package com.example.expenso.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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

    // 🔢 Total amount calculation
    val totalAmount = expenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Expense Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // 💡 Highlighted total card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total Expenses", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "€${"%.2f".format(totalAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (expenses.isEmpty()) {
            Text("No expenses found", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(expenses) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onEditClick = {
                            navController.navigate("edit_expense/${expense.id}")
                        },
                        onDeleteClick = {
                            expenseViewModel.deleteExpense(expense.id)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ExpenseItem(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormatted = expense.date?.toDate()?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
    } ?: "Unknown Date"

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.category, style = MaterialTheme.typography.bodyLarge)
                Text(text = "Amount: €${expense.amount}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Date: $dateFormatted", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Note: ${expense.note}", style = MaterialTheme.typography.bodyMedium)
            }

            Column {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

