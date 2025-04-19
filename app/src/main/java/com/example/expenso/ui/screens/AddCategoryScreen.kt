package com.example.expenso.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expenso.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    navController: NavController,
    categoryViewModel: CategoryViewModel
) {
    var categoryName by remember { mutableStateOf(TextFieldValue("")) }
    var categoryDescription by remember { mutableStateOf(TextFieldValue("")) }
    var categoryImageUrl by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add New Category",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            label = { Text("Category Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = categoryDescription,
            onValueChange = { categoryDescription = it },
            label = { Text("Category Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = categoryImageUrl,
            onValueChange = { categoryImageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val name = categoryName.text.trim()
                val description = categoryDescription.text.trim()
                val imageUrl = categoryImageUrl.text.trim()

                if (name.isNotEmpty()) {
                    categoryViewModel.addCategory(name, description, imageUrl)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Category")
        }
    }
}
