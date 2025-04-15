package com.example.expenso.ui.screens

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
fun SignupScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var termsChecked by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E384D), // Dark blue-gray
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Subtitle
        Text(
            text = "Create your account to start managing your\n" +
                    "finances.",
            fontSize = 16.sp,
            color = Color(0xFF6B7280), // Medium gray
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Your email address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Create a password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Terms Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = termsChecked,
                onCheckedChange = { termsChecked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0x99CE5536)
                )
            )
            Text(
                text = "I agree with Terms & Conditions",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6B7280)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Button
        Button(
            onClick = { if (termsChecked) authViewModel.signUp(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xBFCE5536),
                contentColor = Color.White
            ),
            enabled = termsChecked
        ) {
            Text(text = "Sign Up", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Login Link
        Text(
            text = buildAnnotatedString {
                append("Already registered? ")
                withStyle(style = SpanStyle(
                    color = Color(0xD9EF4444),
                    fontWeight = FontWeight.Bold
                )) {
                    append("Log in")
                }
            },
            modifier = Modifier.clickable { navController.navigate("login") }
        )

        // Auth State Handling
        when (authState) {
            is AuthViewModel.AuthState.Loading -> CircularProgressIndicator()
            is AuthViewModel.AuthState.Success -> {
                Text(
                    text = "Signed up successfully!",
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(top = 16.dp)
                )
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