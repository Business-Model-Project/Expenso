package com.example.expenso.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expenso.viewmodel.CategoryViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    navController: NavController,
    categoryId: String,
    currentName: String,
    currentDescription: String,
    encodedImageUrl: String,
    categoryViewModel: CategoryViewModel
) {
    val decodedImageUrl = URLDecoder.decode(encodedImageUrl, StandardCharsets.UTF_8.toString())

    var newName by remember { mutableStateOf(currentName) }
    var newDescription by remember { mutableStateOf(currentDescription) }
    var newImageUrl by remember { mutableStateOf(decodedImageUrl) }

    val context = LocalContext.current
    val message by categoryViewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("success", ignoreCase = true)) {
                navController.popBackStack()
            }
            categoryViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Category") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newDescription,
                onValueChange = { newDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newImageUrl,
                onValueChange = { newImageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    categoryViewModel.updateCategory(
                        categoryId = categoryId,
                        newName = newName.trim(),
                        newDescription = newDescription.trim(),
                        newImageUrl = newImageUrl.trim()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
