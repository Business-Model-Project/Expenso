package com.example.expenso.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.expenso.data.Category
import com.example.expenso.viewmodel.CategoryViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CategoriesScreen(
    navController: NavController,
    categoryViewModel: CategoryViewModel
) {
    val categories by categoryViewModel.categories.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (categories.isEmpty()) {
            Text("No categories found.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = category.description ?: "No description",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Image(
                                painter = rememberAsyncImagePainter(category.imageUrl),
                                contentDescription = "${category.name} image",
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(end = 4.dp),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                IconButton(
                                    onClick = {
                                        val encodedUrl = URLEncoder.encode(category.imageUrl, StandardCharsets.UTF_8.toString())
                                        navController.navigate(
                                            "edit_category/${category.id}/${category.name}/${category.description ?: ""}/$encodedUrl"
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Category",
                                        tint = Color.Blue
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        categoryToDelete = category
                                        showDeleteDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Category",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("add_category")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Category")
        }
    }

    if (showDeleteDialog && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this category?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.id?.let { categoryId ->
                            categoryViewModel.deleteCategory(categoryId)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
