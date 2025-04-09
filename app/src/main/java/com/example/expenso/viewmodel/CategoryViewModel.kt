package com.example.expenso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.Category
import com.example.expenso.data.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    private val repository = CategoryRepository()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() {
        _message.value = null
    }

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _categories.value = repository.getCategories()
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.addCategory(name)
            fetchCategories()
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
            fetchCategories()
        }
    }

    fun updateCategory(categoryId: String, newName: String) {
        viewModelScope.launch {
            if (newName.isBlank()) {
                _message.value = "Category name cannot be empty"
                return@launch
            }

            repository.updateCategory(categoryId, newName)
            _message.value = "Category updated successfully"
            fetchCategories()
        }
    }
}
