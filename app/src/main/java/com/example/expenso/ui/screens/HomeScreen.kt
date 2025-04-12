package com.example.expenso.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expenso.data.Expense
import com.example.expenso.viewmodel.CategoryViewModel
import com.example.expenso.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val allExpenses by expenseViewModel.expenses.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var selectedFilter by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("") }

    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }

    val filteredExpenses = allExpenses.filter { expense ->
        val expenseDate = expense.date?.toDate() ?: return@filter false
        val cal = Calendar.getInstance()

        when (selectedFilter) {
            "Monthly" -> {
                val now = Calendar.getInstance()
                cal.time = expenseDate
                cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            }

            "Weekly" -> {
                val now = Calendar.getInstance()
                cal.time = expenseDate
                cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            }

            "Range" -> {
                if (startDate != null && endDate != null) {
                    expenseDate in startDate!!..endDate!!
                } else true
            }

            else -> true
        } && (selectedCategory.isBlank() || expense.category == selectedCategory)
    }.sortedByDescending { it.date }

    val totalAmount = filteredExpenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Expense Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total: €${"%.2f".format(totalAmount)}", style = MaterialTheme.typography.headlineSmall)
            }
        }

        FilterChipsRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CategoryDropdown(
            categories = categories.map { it.name },
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedFilter == "Range") {
            DateRangeSelector(
                startDate = startDate,
                endDate = endDate,
                onStartSelected = { startDate = it },
                onEndSelected = { endDate = it },
                onReset = {
                    startDate = null
                    endDate = null
                },
                formatter = dateFormatter
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredExpenses.isEmpty()) {
            Text("No expenses found", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(filteredExpenses) { expense ->
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
fun FilterChipsRow(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All", "Monthly", "Weekly", "Range")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        filters.forEach { label ->
            FilterChip(
                selected = selectedFilter == label,
                onClick = { onFilterSelected(label) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val allOptions = listOf("All Categories") + categories
    val selectedText = if (selectedCategory.isBlank()) "All Categories" else selectedCategory

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            allOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onCategorySelected(if (option == "All Categories") "" else option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateRangeSelector(
    startDate: Date?,
    endDate: Date?,
    onStartSelected: (Date) -> Unit,
    onEndSelected: (Date) -> Unit,
    onReset: () -> Unit,
    formatter: SimpleDateFormat
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val startPicker = DatePickerDialog(
        context,
        { _, y, m, d -> onStartSelected(Calendar.getInstance().apply { set(y, m, d) }.time) },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val endPicker = DatePickerDialog(
        context,
        { _, y, m, d -> onEndSelected(Calendar.getInstance().apply { set(y, m, d) }.time) },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = { startPicker.show() }) {
            Text(text = startDate?.let { "From: ${formatter.format(it)}" } ?: "From")
        }

        Button(onClick = { endPicker.show() }) {
            Text(text = endDate?.let { "To: ${formatter.format(it)}" } ?: "To")
        }

        if (startDate != null || endDate != null) {
            TextButton(onClick = { onReset() }) {
                Text("Reset")
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
                Text(text = expense.category, style = MaterialTheme.typography.titleLarge)
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
