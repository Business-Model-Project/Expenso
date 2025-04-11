package com.example.expenso.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Timestamp? = null,
    val note: String = ""
)

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun addExpense(expense: Expense) {
        val userId = getUserId() ?: return
        db.collection("users").document(userId)
            .collection("expenses")
            .add(expense)
    }

    fun observeExpenses(onExpensesChanged: (List<Expense>) -> Unit) {
        val userId = getUserId() ?: return

        db.collection("users").document(userId)
            .collection("expenses")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Expense::class.java)?.copy(id = doc.id)
                    }
                    onExpensesChanged(expenses) // 🔥 Real-time update
                }
            }
    }
    suspend fun updateExpense(expense: Expense) {
        val userId = getUserId() ?: return
        db.collection("users").document(userId)
            .collection("expenses")
            .document(expense.id)
            .set(expense)  // Overwrites existing doc with updated data
    }

    suspend fun deleteExpense(expenseId: String) {
        val userId = getUserId() ?: return
        db.collection("users").document(userId)
            .collection("expenses")
            .document(expenseId)
            .delete()
    }
}

