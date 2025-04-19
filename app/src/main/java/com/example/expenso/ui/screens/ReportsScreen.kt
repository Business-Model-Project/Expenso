package com.example.expenso.ui.screens

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@Composable
fun ReportsScreen(
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val expenses by expenseViewModel.expenses.collectAsState()
    val context = LocalContext.current

    // Process data for charts
    val (barEntries, barLabels) = remember(expenses) { processBarData(expenses) }
    val pieEntries = remember(expenses) { processPieData(expenses) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Expense Report", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Bar Chart
        Text("Monthly Expenses", style = MaterialTheme.typography.titleMedium)
        AndroidView(
            factory = { context ->
                BarChart(context).apply {
                    configureBarChart(barLabels)  // Pass labels here
                    data = BarData(
                        BarDataSet(barEntries, "Monthly Expenses").apply {
                            color = Color.parseColor("#2196F3")
                            valueTextColor = Color.BLACK
                            valueFormatter = CurrencyFormatter()
                        }
                    )
                    animateY(1000)
                    invalidate()
                }
            },
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Category Pie Chart
        Text("Expense Categories", style = MaterialTheme.typography.titleMedium)
        AndroidView(
            factory = { context ->
                PieChart(context).apply {
                    configurePieChart()
                    data = PieData(
                        PieDataSet(pieEntries, "Expense Categories").apply {
                            colors = getColorPalette()
                            valueTextColor = Color.WHITE
                            valueFormatter = PercentageFormatter()
                        }
                    )
                    animateXY(1000, 1000)
                    invalidate()
                }
            },
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        )
    }
}

// Data processing functions
private fun processBarData(expenses: List<Expense>): Pair<ArrayList<BarEntry>, List<String>> {
    val grouped = expenses.groupBy {
        it.date?.toDate()?.let { date ->
            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
        } ?: "Unknown"
    }

    val sortedEntries = grouped.entries.sortedBy {
        it.key.let { key ->
            SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(key)
        }
    }

    val entries = ArrayList<BarEntry>()
    val labels = ArrayList<String>()

    sortedEntries.forEachIndexed { index, entry ->
        val total = entry.value.sumOf { it.amount }
        entries.add(BarEntry(index.toFloat(), total.toFloat()))
        labels.add(entry.key)
    }

    return Pair(entries, labels)
}

private fun processPieData(expenses: List<Expense>): ArrayList<PieEntry> {
    val grouped = expenses.groupBy { it.category }
    return ArrayList(grouped.map {
        PieEntry(it.value.sumOf { expense -> expense.amount }.toFloat(), it.key)
    })
}

// Chart configuration (modified to accept barLabels parameter)
private fun BarChart.configureBarChart(barLabels: List<String>) {
    description.isEnabled = false
    legend.isEnabled = false
    setTouchEnabled(false)

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = IndexAxisValueFormatter(barLabels.toTypedArray()) // Convert to array
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
    setEntryLabelColor(Color.WHITE)
    setHoleColor(Color.TRANSPARENT)
    setTransparentCircleAlpha(0)
    setDrawEntryLabels(true)
}

// Formatters
class CurrencyFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "₹${String.format(Locale.ENGLISH, "%.2f", abs(value))}"
    }
}

class PercentageFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${String.format(Locale.ENGLISH, "%.1f", value)}%"
    }
}

// Color palette for pie chart
private fun getColorPalette(): List<Int> {
    return listOf(
        Color.parseColor("#FF6384"),
        Color.parseColor("#36A2EB"),
        Color.parseColor("#FFCE56"),
        Color.parseColor("#4BC0C0"),
        Color.parseColor("#9966FF"),
        Color.parseColor("#FF9F40")
        )
}