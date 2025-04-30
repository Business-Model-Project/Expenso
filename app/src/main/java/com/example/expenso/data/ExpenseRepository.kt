package com.example.expenso.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// -------------------------
// Data Model
// -------------------------
data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Timestamp? = null,
    val note: String = ""
)

// -------------------------
// Repository
// -------------------------
class ExpenseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    // Add a new expense
    suspend fun addExpense(expense: Expense) {
        val userId = getUserId() ?: return
        db.collection("users")
            .document(userId)
            .collection("expenses")
            .add(expense)
    }

    // Observe real-time expense changes
    fun observeExpenses(onExpensesChanged: (List<Expense>) -> Unit) {
        val userId = getUserId() ?: return

        db.collection("users")
            .document(userId)
            .collection("expenses")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Expense::class.java)?.copy(id = doc.id)
                    }
                    onExpensesChanged(expenses)
                }
            }
    }

    // Update an existing expense
    suspend fun updateExpense(expense: Expense) {
        val userId = getUserId() ?: return

        db.collection("users")
            .document(userId)
            .collection("expenses")
            .document(expense.id)
            .set(expense)
    }

    // Delete an expense by ID
    suspend fun deleteExpense(expenseId: String) {
        val userId = getUserId() ?: return

        db.collection("users")
            .document(userId)
            .collection("expenses")
            .document(expenseId)
            .delete()
    }
}