package com.example.expenso.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

@Composable
fun SettingsScreen(onLogoutSuccess: () -> Unit) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var monthlyBudget by remember { mutableStateOf(TextFieldValue("")) }
    var notifyOnExceed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Load existing budget when screen opens
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            doc.getDouble("budget")?.let {
                monthlyBudget = TextFieldValue(it.toString())
            }
            doc.getBoolean("notifyOnExceed")?.let {
                notifyOnExceed = it
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading settings", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Budget Settings", style = MaterialTheme.typography.titleLarge)

        // Notify switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Notify me when I exceed budget", modifier = Modifier.weight(1f))
            Switch(
                checked = notifyOnExceed,
                onCheckedChange = { notifyOnExceed = it }
            )
        }

        // Budget input
        OutlinedTextField(
            value = monthlyBudget,
            onValueChange = { newValue ->
                if (newValue.text.isEmpty() || newValue.text.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    monthlyBudget = newValue
                }
            },
            label = { Text("Monthly Budget (€)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val budgetValue = monthlyBudget.text.toDoubleOrNull()
                if (budgetValue == null || budgetValue < 0) {
                    Toast.makeText(context, "Please enter a valid positive number", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                try {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set(
                            mapOf(
                                "budget" to budgetValue,
                                "notifyOnExceed" to notifyOnExceed,
                                "lastUpdated" to FieldValue.serverTimestamp()
                            ),
                            SetOptions.merge()
                        )
                        .addOnSuccessListener {
                            Toast.makeText(context, "Budget settings saved!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Budget Settings")
        }

        Divider()

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Log Out")
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        showLogoutDialog = false
                        onLogoutSuccess()
                    }
                ) {
                    Text("Yes, Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}