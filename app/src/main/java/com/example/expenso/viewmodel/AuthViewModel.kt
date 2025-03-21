package com.example.expenso.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Success
                    } else {
                        Log.e("AuthError", "Login failed: ${task.exception?.message}")
                        _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                    }
                }
        } catch (e: Exception) {
            Log.e("AuthError", "Login exception: ${e.message}")
            _authState.value = AuthState.Error(e.message ?: "Unexpected error")
        }
    }

    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Success
                    } else {
                        Log.e("AuthError", "Signup failed: ${task.exception?.message}")
                        _authState.value = AuthState.Error(task.exception?.message ?: "Signup failed")
                    }
                }
        } catch (e: Exception) {
            Log.e("AuthError", "Signup exception: ${e.message}")
            _authState.value = AuthState.Error(e.message ?: "Unexpected error")
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _authState.value = AuthState.Idle
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
