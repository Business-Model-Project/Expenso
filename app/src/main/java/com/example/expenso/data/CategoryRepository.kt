package com.example.expenso.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = ""
)

class CategoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun addCategory(name: String, description: String, imageUrl: String) {
        val userId = getUserId() ?: return
        val category = hashMapOf(
            "name" to name,
            "description" to description,
            "imageUrl" to imageUrl
        )
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
            val description = doc.getString("description") ?: ""
            val imageUrl = doc.getString("imageUrl") ?: ""
            Category(id = doc.id, name = name, description = description, imageUrl = imageUrl)
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

    suspend fun updateCategory(categoryId: String, newName: String, newDescription: String, newImageUrl: String) {
        val userId = getUserId() ?: return
        val updates = mapOf(
            "name" to newName,
            "description" to newDescription,
            "imageUrl" to newImageUrl
        )
        db.collection("users").document(userId)
            .collection("categories")
            .document(categoryId)
            .update(updates)
            .await()
    }
}
