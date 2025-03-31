package com.example.expenso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.Expense
import com.example.expenso.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {
    private val repository = ExpenseRepository()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    init {
        fetchExpenses()
    }

    fun fetchExpenses() {
        viewModelScope.launch {
            _expenses.value = repository.getExpenses()
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
            fetchExpenses() // always Refreshes list after adding
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
            fetchExpenses() // always Refreshes list after deleting
        }
    }
}
