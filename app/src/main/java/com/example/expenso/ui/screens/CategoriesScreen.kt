package com.example.expenso.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (categories.isEmpty()) {
                item {
                    Text(
                        text = "No categories found.",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = {
                            val encodedUrl = URLEncoder.encode(
                                category.imageUrl,
                                StandardCharsets.UTF_8.toString()
                            )
                            navController.navigate(
                                "edit_category/${category.id}/" +
                                        "${category.name}/${category.description ?: ""}/$encodedUrl"
                            )
                        },
                        onDelete = {
                            categoryToDelete = category
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { navController.navigate("add_category") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Category",
                tint = Color.White
            )
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
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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

@Composable
private fun CategoryCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Image Section
            Image(
                painter = rememberAsyncImagePainter(category.imageUrl),
                contentDescription = "${category.name} image",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
            )

            // Text Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.description ?: "No description",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}