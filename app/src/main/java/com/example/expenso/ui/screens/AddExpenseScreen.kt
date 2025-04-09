package com.example.expenso.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expenso.data.Expense
import com.example.expenso.viewmodel.ExpenseViewModel
import com.example.expenso.viewmodel.CategoryViewModel
import com.google.firebase.Timestamp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onExpenseAdded: () -> Unit,
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<Timestamp?>(null) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val categories by categoryViewModel.categories.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            calendar.set(year, month, day)
            date = Timestamp(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Add Expense", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category dropdown
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                label = { Text("Category") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (categories.isNotEmpty()) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category.name
                                expanded = false
                            }
                        )
                    }
                } else {
                    DropdownMenuItem(
                        text = { Text("Go to Add Category") },
                        onClick = {
                            // You can navigate to add_category if needed here
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = date?.toDate()?.toString() ?: "Select Date",
            onValueChange = { },
            label = { Text("Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Select Date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (amount.isNotEmpty() && selectedCategory.isNotEmpty() && date != null) {
                    expenseViewModel.addExpense(
                        Expense(
                            amount = amount.toDouble(),
                            category = selectedCategory,
                            date = date,
                            note = note
                        )
                    )
                    onExpenseAdded()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Expense")
        }
    }
}
