package com.example.expenso.ui.screens

import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.DefaultTab.AlbumsTab.value
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expenso.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            text = "Log In",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(bottom = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xD9CE5536),
                contentColor = Color.White
            )
        ) {
            Text(text = "Log In", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 16.dp),
            thickness = 1.dp,
            color = Color(0xFFE5E7EB)
        )

        // Sign Up Link
        Text(
            text = buildAnnotatedString {
                append("Don't have an account? ")
                withStyle(style = SpanStyle(
                    color = Color(0xD9EF4444),
                    fontWeight = FontWeight.Bold
                )) {
                    append("Sign Up")
                }
            },
            modifier = Modifier.clickable { navController.navigate("signup") }
        )

        // Auth State Handling
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                LaunchedEffect(Unit) {
                    navController.navigate("home") {
                        popUpTo("landing") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthViewModel.AuthState.Error -> {
                Text(
                    text = (authState as AuthViewModel.AuthState.Error).message,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            else -> {}
                }
        }
}