package com.example.expenso.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expenso.utils.NotificationUtils
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onLogoutSuccess: () -> Unit) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var monthlyBudget by remember { mutableStateOf("") }
    var notifyOnExceed by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showPasswordForm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = Firebase.auth

    // Track if we've loaded initial settings
    var hasLoadedInitialSettings by remember { mutableStateOf(false) }

    // Load user data when screen opens
    LaunchedEffect(Unit) {
        if (hasLoadedInitialSettings) return@LaunchedEffect

        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        try {
            // Load budget settings
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            doc.getDouble("budget")?.let {
                monthlyBudget = it.toString()
            }
            // Load notification preference, default to false if not set
            notifyOnExceed = doc.getBoolean("notifyOnExceed") ?: false

            // Load email from auth
            email = auth.currentUser?.email ?: ""

            hasLoadedInitialSettings = true
        } catch (e: Exception) {
            // If document doesn't exist, use defaults
            monthlyBudget = ""
            notifyOnExceed = false
            hasLoadedInitialSettings = true
            Toast.makeText(context, "Error loading settings", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Account Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Account Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Email Field (read-only)
                OutlinedTextField(
                    value = email,
                    onValueChange = { },
                    label = { Text(text = "Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = false,
                    shape = RoundedCornerShape(8.dp)
                )

                // Change Password Button
                FilledTonalButton(
                    onClick = { showPasswordForm = !showPasswordForm },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = if (showPasswordForm) "Cancel" else "Change Password")
                }

                // Password Form (only visible when showPasswordForm is true)
                AnimatedVisibility(
                    visible = showPasswordForm,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current Password
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text(text = "Current Password") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = "Current Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            }
                        )

                        // New Password
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(text = "New Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "New Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        // Confirm New Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(text = "Confirm New Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        Button(
                            onClick = {
                                if (isLoading) return@Button

                                if (currentPassword.isEmpty()) {
                                    Toast.makeText(context, "Please enter current password", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (newPassword != confirmPassword) {
                                    Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (newPassword.length < 6) {
                                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isLoading = true
                                changePassword(
                                    email = email,
                                    currentPassword = currentPassword,
                                    newPassword = newPassword,
                                    onSuccess = {
                                        isLoading = false
                                        showPasswordForm = false
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        NotificationUtils.sendPasswordChangedNotification(context)
                                        Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(text = "Update Password")
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Budget Settings Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Budget Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Budget input
                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            monthlyBudget = newValue
                        }
                    },
                    label = { Text(text = "Monthly Budget (€)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                // Notify switch with persistent state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Notify me when I exceed budget",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = notifyOnExceed,
                        onCheckedChange = { newValue ->
                            notifyOnExceed = newValue
                            // Immediately save the change
                            saveBudgetSettings(
                                monthlyBudget = monthlyBudget,
                                notifyOnExceed = newValue,
                                onSuccess = {
                                    Toast.makeText(context, "Notification preference saved", Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, "Failed to save: $error", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }

                Button(
                    onClick = {
                        val budgetValue = monthlyBudget.toDoubleOrNull()
                        if (budgetValue == null && monthlyBudget.isNotEmpty()) {
                            Toast.makeText(context, "Please enter a valid positive number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        saveBudgetSettings(
                            monthlyBudget = monthlyBudget,
                            notifyOnExceed = notifyOnExceed,
                            onSuccess = {
                                Toast.makeText(context, "Budget settings saved!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Failed to save: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save Budget Settings")
                }
            }

            HorizontalDivider()

            // Logout section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = "Log Out")
                }
            }
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Yes, Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

private fun saveBudgetSettings(
    monthlyBudget: String,
    notifyOnExceed: Boolean,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
        onError("User not authenticated")
        return
    }

    try {
        val budgetValue = monthlyBudget.toDoubleOrNull() ?: 0.0

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
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Unknown error")
            }
    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
    }
}

private fun changePassword(
    email: String,
    currentPassword: String,
    newPassword: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val user = Firebase.auth.currentUser
    val credential = EmailAuthProvider.getCredential(email, currentPassword)

    user?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
        if (reauthTask.isSuccessful) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        onSuccess()
                    } else {
                        onError(updateTask.exception?.message ?: "Password update failed")
                    }
                }
        } else {
            onError(reauthTask.exception?.message ?: "Authentication failed")
        }
    } ?: onError("User not authenticated")
}