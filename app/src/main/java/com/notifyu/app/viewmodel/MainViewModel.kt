package com.notifyu.app.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUp(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true,"Sign Up Successful")
                    val user = task.result?.user
                    Log.d("FirebaseAuth", "Sign Up Successful: ${user?.email}")
                } else {
                    onResult(false, "${task.exception?.message}")
                    Log.e("FirebaseAuth", "Sign Up Failed: ${task.exception?.message}")
                }
            }
    }
    fun sendEmailVerification(onResult: (Boolean, String) -> Unit){
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true,"Verification email sent")
                    Log.d("EmailVerification", "Verification email sent to ${auth.currentUser?.email}")
                } else {
                    onResult(false,"Failed to send verification email")
                    Log.e("EmailVerification", "Failed to send verification email", task.exception)
                }
            }
    }
    fun checkEmailVerification(onResult: (Boolean) -> Unit) {
        auth.currentUser?.reload()?.addOnCompleteListener {
            if (it.isSuccessful) {
                onResult(auth.currentUser!!.isEmailVerified)
                Log.d("EmailVerification", "checkEmailVerification ${auth.currentUser!!.isEmailVerified}")
            } else {
                onResult(false)
            }
        }
    }

    fun loginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        Log.d("LoginDebug", "Attempting login with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("LoginDebug", "Login successful. User: ${user?.email}, Verified: ${user?.isEmailVerified}")
                    onResult(true)
                } else {
                    Log.e("LoginDebug", "Login failed", task.exception)
                    onResult(false)
                }
            }
    }

    fun updatePassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, "Password updated successfully")
                    } else {
                        onResult(false, task.exception?.message ?: "Password update failed")
                    }
                }
        } else {
            onResult(false, "No user is currently signed in")
        }
    }



}