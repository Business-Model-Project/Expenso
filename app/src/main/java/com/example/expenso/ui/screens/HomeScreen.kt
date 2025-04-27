package com.example.expenso.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expenso.data.Expense
import com.example.expenso.utils.NotificationUtils
import com.example.expenso.viewmodel.CategoryViewModel
import com.example.expenso.viewmodel.ExpenseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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
    var showFilters by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }

    val filteredExpenses = allExpenses.filter { expense ->
        val expenseDate = expense.date?.toDate() ?: return@filter false
        val cal = Calendar.getInstance()

        val matchesFilter = when (selectedFilter) {
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
        }

        val matchesCategory = selectedCategory.isBlank() || expense.category == selectedCategory

        matchesFilter && matchesCategory
    }.sortedByDescending { it.date }

    val totalAmount = filteredExpenses.sumOf { it.amount }

    val context = LocalContext.current

    // Budget tracking state
    var budget by remember { mutableStateOf(0.0) }
    var notifyOnExceed by remember { mutableStateOf(false) }
    var notificationSentThisSession by remember { mutableStateOf(false) }

    // Load budget settings from Firestore
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            doc.getDouble("budget")?.let { budget = it }
            doc.getBoolean("notifyOnExceed")?.let { notifyOnExceed = it }
        } catch (e: Exception) {
            // Silent fail - budget features will be disabled
        }
    }

    // Budget notification logic
    LaunchedEffect(totalAmount, notifyOnExceed) {
        if (notifyOnExceed && budget > 0 && totalAmount > budget) {
            if (!notificationSentThisSession) {
                NotificationUtils.sendBudgetExceededNotification(context, totalAmount, budget)
                notificationSentThisSession = true
            }
        } else if (totalAmount <= budget * 0.9) { // Reset if spending drops below 90% of budget
            notificationSentThisSession = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Budget status card
            BudgetStatusCard(totalAmount, budget)

            Spacer(modifier = Modifier.height(8.dp))

            // Filters section
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FiltersSection(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    categories = categories.map { it.name },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active filters display
            if (selectedFilter != "All" || selectedCategory.isNotBlank()) {
                ActiveFiltersDisplay(
                    selectedFilter = selectedFilter,
                    selectedCategory = selectedCategory,
                    startDate = startDate,
                    endDate = endDate,
                    dateFormatter = dateFormatter,
                    onClearAll = {
                        selectedFilter = "All"
                        selectedCategory = ""
                        startDate = null
                        endDate = null
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Expenses list
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No expenses found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Try adjusting your filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    "${filteredExpenses.size} expense${if (filteredExpenses.size != 1) "s" else ""} found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
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
}

@Composable
private fun BudgetStatusCard(totalAmount: Double, budget: Double) {
    val budgetPercentage = if (budget > 0) (totalAmount / budget * 100) else 0.0
    val cardColor = when {
        budget > 0 && totalAmount > budget -> MaterialTheme.colorScheme.errorContainer
        budget > 0 && budgetPercentage > 80 -> MaterialTheme.colorScheme.tertiaryContainer
        budget > 0 -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        budget > 0 && totalAmount > budget -> MaterialTheme.colorScheme.onErrorContainer
        budget > 0 && budgetPercentage > 80 -> MaterialTheme.colorScheme.onTertiaryContainer
        budget > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Total Expenses",
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
            Text(
                "€${String.format("%.2f", totalAmount)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            if (budget > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = (totalAmount / budget).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        budgetPercentage > 100 -> MaterialTheme.colorScheme.error
                        budgetPercentage > 80 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = Color.Black.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Budget: €${String.format("%.2f", budget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                    Text(
                        "${String.format("%.1f", budgetPercentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }

                if (totalAmount > budget) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Over budget by €${String.format("%.2f", totalAmount - budget)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltersSection(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    startDate: Date?,
    endDate: Date?,
    onStartSelected: (Date) -> Unit,
    onEndSelected: (Date) -> Unit,
    onReset: () -> Unit,
    formatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time period filters
            Column {
                Text(
                    "Time Period",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FilterChipsRow(selectedFilter, onFilterSelected)
            }

            // Category filter
            Column {
                Text(
                    "Category",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                CategoryDropdown(categories, selectedCategory, onCategorySelected)
            }

            // Date range selector (only show when Range filter is selected)
            AnimatedVisibility(visible = selectedFilter == "Range") {
                Column {
                    Text(
                        "Date Range",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DateRangeSelector(
                        startDate = startDate,
                        endDate = endDate,
                        onStartSelected = onStartSelected,
                        onEndSelected = onEndSelected,
                        onReset = onReset,
                        formatter = formatter
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All", "Monthly", "Weekly", "Range")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { label ->
            FilterChip(
                selected = selectedFilter == label,
                onClick = { onFilterSelected(label) },
                label = { Text(label) },
                leadingIcon = if (selectedFilter == label) {
                    { Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun ActiveFiltersDisplay(
    selectedFilter: String,
    selectedCategory: String,
    startDate: Date?,
    endDate: Date?,
    dateFormatter: SimpleDateFormat,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Active Filters:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (selectedFilter != "All") {
                SuggestionChip(
                    onClick = { },
                    label = { Text(selectedFilter) }
                )
            }

            if (selectedCategory.isNotBlank()) {
                SuggestionChip(
                    onClick = { },
                    label = { Text(selectedCategory) },
                    icon = { Icon(Icons.Outlined.Category, null, modifier = Modifier.size(16.dp)) }
                )
            }

            if (selectedFilter == "Range" && startDate != null && endDate != null) {
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            "${dateFormatter.format(startDate)} - ${dateFormatter.format(endDate)}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        TextButton(onClick = onClearAll) {
            Text("Clear All")
        }
    }
}

@Composable
private fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val allOptions = listOf("All Categories") + categories
    val selectedText = if (selectedCategory.isBlank()) "All Categories" else selectedCategory

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedText)
                Icon(Icons.Outlined.Category, contentDescription = null)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
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
private fun DateRangeSelector(
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { startPicker.show() },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = startDate?.let { formatter.format(it) } ?: "Start Date",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = { endPicker.show() },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = endDate?.let { formatter.format(it) } ?: "End Date",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (startDate != null || endDate != null) {
            TextButton(
                onClick = onReset,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Reset Dates")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseItem(
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
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onEditClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "€${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (expense.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}