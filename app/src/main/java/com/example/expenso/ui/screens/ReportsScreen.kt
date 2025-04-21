package com.example.expenso.ui.screens

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expenso.data.Expense
import com.example.expenso.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import androidx.compose.ui.graphics.Color as ComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val expenses by expenseViewModel.expenses.collectAsState()
    val context = LocalContext.current

    // Filter states
    var selectedTimeFilter by remember { mutableStateOf("ALL") }
    var selectedCategory by remember { mutableStateOf("") }
    val timeFilters = listOf("ALL", "Monthly", "Weekly", "Range")
    val categories = remember { expenses.map { it.category }.distinct() }

    // Date range states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }

    // Filtered expenses
    val filteredExpenses = remember(expenses, selectedTimeFilter, selectedCategory, startDate, endDate) {
        expenses.filter { expense ->
            (selectedCategory.isEmpty() || expense.category == selectedCategory) &&
                    filterByDate(expense.date?.toDate(), selectedTimeFilter, startDate, endDate)
        }
    }

    // Process data for charts
    val (barEntries, barLabels) = remember(filteredExpenses, selectedTimeFilter) {
        processBarData(filteredExpenses, selectedTimeFilter)
    }
    val (pieEntries, totalSum) = remember(filteredExpenses) { processPieData(filteredExpenses) }
    val colorPalette = remember { getColorPalette().map { ComposeColor(it) } }

    // Category-color pairs for legend
    val categoriesWithColors = remember(pieEntries) {
        pieEntries.mapIndexed { index, entry ->
            Pair(
                entry.label ?: "Uncategorized",
                colorPalette.getOrNull(index % colorPalette.size) ?: ComposeColor.Black
            )
        }
    }

    // Date pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismiss = { showStartDatePicker = false },
            onConfirm = { date ->
                startDate = date
                showStartDatePicker = false
                showEndDatePicker = true
            }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismiss = {
                showEndDatePicker = false
                startDate = null
                endDate = null
                selectedTimeFilter = "ALL"
            },
            onConfirm = { date ->
                endDate = date
                showEndDatePicker = false
                selectedTimeFilter = "Range"
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp))
            {
                Text("Expense Report", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))

                // Time filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(timeFilters) { filter ->
                        FilterChip(
                            selected = selectedTimeFilter == filter,
                            onClick = {
                                if (filter == "Range") {
                                    showStartDatePicker = true
                                } else {
                                    selectedTimeFilter = filter
                                    startDate = null
                                    endDate = null
                                }
                            },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Category filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) "" else category
                            },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Date range display
                if (selectedTimeFilter == "Range" && startDate != null && endDate != null) {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    Text(
                        text = "Selected Range: ${dateFormat.format(startDate!!)} - ${dateFormat.format(endDate!!)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Bar Chart
                Text("Expense Trend", style = MaterialTheme.typography.titleMedium)
                AndroidView(
                    factory = { BarChart(context) },
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    update = { chart ->
                        chart.configureBarChart(barLabels)
                        chart.data = BarData(
                            BarDataSet(barEntries, "Expenses").apply {
                                color = Color.parseColor("#2196F3")
                                valueTextColor = Color.BLACK
                                valueFormatter = CurrencyFormatter()
                            }
                        )
                        chart.animateY(1000)
                        chart.invalidate()
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Pie Chart
                Text("Category Breakdown", style = MaterialTheme.typography.titleMedium)
                AndroidView(
                    factory = { PieChart(context) },
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    update = { chart ->
                        chart.configurePieChart()
                        chart.data = PieData(
                            PieDataSet(pieEntries, "").apply {
                                colors = colorPalette.map { it.toArgb() }
                                valueTextColor = Color.BLACK
                                valueTextSize = 12f
                                valueFormatter = PercentageFormatter(totalSum)
                            }
                        )
                        chart.animateXY(1000, 1000)
                        chart.invalidate()
                    }
                )

                // Category Legend
                Spacer(Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoriesWithColors) { (category, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color = color)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = color
                            )
                        }
                    }
                }
            }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                showDialog = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onConfirm(Date(it))
                        }
                        showDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        showDialog = false
                    }
                ) { Text("Cancel") }
            },
            title = { Text("Select Date") },
            text = {
                DatePicker(
                    state = datePickerState,
                    title = null
                )
            }
        )
    }
}

private fun filterByDate(
    date: Date?,
    filter: String,
    startDate: Date? = null,
    endDate: Date? = null
): Boolean {
    if (date == null) return false
    val calDate = Calendar.getInstance().apply { time = date }
    val calStart = startDate?.let { Calendar.getInstance().apply { time = it } }
    val calEnd = endDate?.let { Calendar.getInstance().apply { time = it } }

    return when (filter) {
        "Weekly" -> calDate.get(Calendar.WEEK_OF_YEAR) == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) &&
                calDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
        "Monthly" -> calDate.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                calDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
        "Range" -> (calStart == null || calDate.time >= calStart.time) &&
                (calEnd == null || calDate.time <= calEnd.time)
        else -> true
    }
}

private fun processBarData(expenses: List<Expense>, filter: String): Pair<ArrayList<BarEntry>, List<String>> {
    val format = when (filter) {
        "Weekly" -> "ww"
        "Monthly" -> "MMM"
        else -> "dd MMM"
    }
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())

    val grouped = expenses.groupBy {
        it.date?.toDate()?.let { dateFormat.format(it) } ?: "Unknown"
    }

    val sorted = grouped.entries.sortedBy {
        it.key.let { dateFormat.parse(it)?.time ?: 0L }
    }

    return Pair(
        ArrayList(sorted.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.sumOf { it.amount }.toFloat())
        }),
        sorted.map { it.key }
    )
}

private fun processPieData(expenses: List<Expense>): Pair<ArrayList<PieEntry>, Float> {
    val grouped = expenses.groupBy { it.category }
    val total = expenses.sumOf { it.amount }.toFloat()
    return Pair(
        ArrayList(grouped.map { (k, v) ->
            PieEntry(v.sumOf { it.amount }.toFloat(), k)
        }),
        total
    )
}

private fun BarChart.configureBarChart(labels: List<String>) {
    description.isEnabled = false
    legend.isEnabled = false
    setTouchEnabled(false)

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = IndexAxisValueFormatter(labels)
        granularity = 1f
        setAvoidFirstLastClipping(true)
    }

    axisLeft.apply {
        valueFormatter = CurrencyFormatter()
        axisMinimum = 0f
    }
    axisRight.isEnabled = false
}

private fun PieChart.configurePieChart() {
    description.isEnabled = false
    legend.isEnabled = false
    setEntryLabelColor(Color.BLACK)
    setHoleColor(Color.TRANSPARENT)
    setTransparentCircleAlpha(0)
    setDrawEntryLabels(true)
    setUsePercentValues(false)
}

class CurrencyFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "€${String.format(Locale.ENGLISH, "%.2f", abs(value))}"
    }
}

class PercentageFormatter(private val total: Float) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (total == 0f) "0%" else "${(value / total * 100).toInt()}%"
    }
}

private fun getColorPalette() = listOf(
    Color.parseColor("#FF6384"), Color.parseColor("#36A2EB"),
    Color.parseColor("#FFCE56"), Color.parseColor("#4BC0C0"),
    Color.parseColor("#9966FF"), Color.parseColor("#FF9F40"),
    Color.parseColor("#2ECC71"), Color.parseColor("#E74C3C"),
    Color.parseColor("#9B59B6"), Color.parseColor("#34495E")
)