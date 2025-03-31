package com.example.expenso.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable  // ✅ Add this import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expenso.viewmodel.AuthViewModel

@Composable
fun SignupScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create an Account", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { authViewModel.signUp(email, password) }) {
            Text("Sign Up")
        }

        when (authState) {
            is AuthViewModel.AuthState.Loading -> CircularProgressIndicator()
            is AuthViewModel.AuthState.Success -> {

                Text(text = "Signed up successfully! Go back to the login page.", color = Color.Green)
            }
            is AuthViewModel.AuthState.Error -> {
                Text(text = (authState as AuthViewModel.AuthState.Error).message, color = Color.Red)
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))


        Text(
            text = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Login")
                }
            },
            modifier = Modifier.clickable { navController.navigate("login") }
        )
    }
}
