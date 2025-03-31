package com.example.expenso.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Timestamp? = null,  // ✅ Uses Firestore Timestamp
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
            .await()
    }

    suspend fun getExpenses(): List<Expense> {
        val userId = getUserId() ?: return emptyList()
        val snapshot = db.collection("users").document(userId)
            .collection("expenses")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Expense::class.java)?.copy(id = doc.id) ?: Expense(id = doc.id)
        }
    }

    suspend fun deleteExpense(expenseId: String) {
        val userId = getUserId() ?: return
        db.collection("users").document(userId)
            .collection("expenses")
            .document(expenseId)
            .delete()
            .await()
    }
}
