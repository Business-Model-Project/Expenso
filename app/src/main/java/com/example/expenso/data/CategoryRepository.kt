package com.example.expenso.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Category(
    val id: String = "",
    val name: String = ""
)

class CategoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun addCategory(name: String) {
        val userId = getUserId() ?: return
        val category = hashMapOf("name" to name)
        db.collection("users").document(userId)
            .collection("categories")
            .add(category)
            .await()
    }

    suspend fun getCategories(): List<Category> {
        val userId = getUserId() ?: return emptyList()
        val snapshot = db.collection("users").document(userId)
            .collection("categories")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val name = doc.getString("name") ?: return@mapNotNull null
            Category(id = doc.id, name = name)
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        val userId = getUserId() ?: return
        db.collection("users").document(userId)
            .collection("categories")
            .document(categoryId)
            .delete()
            .await()
    }
}
