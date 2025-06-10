package com.notifyu.app.presentation.screens.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.R
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.screens.components.AsyncProgressDialog
import com.notifyu.app.presentation.screens.auth.components.LottieAnimations
import com.notifyu.app.presentation.screens.auth.components.PrimaryAuthButton
import com.notifyu.app.presentation.screens.components.ValidatedTextField
import com.notifyu.app.presentation.theme.BackgroundColor
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import com.notifyu.app.presentation.viewmodel.states.UiState

@Composable
fun ResetPasswordScreen(navController: NavController, mainViewModel: MainViewModel) {

    // Get the current context to show Toasts
    val context = LocalContext.current

    // State for email input field
    val email = remember { mutableStateOf("") }

    // Observe validation error state from ViewModel for email
    val emailError by mainViewModel.emailValidationError.collectAsState()

    // Observe UI state for reset password operation
    val resetPasswordState by mainViewModel.resetPasswordState.collectAsState()

    // Observe navigation events
    val navEvent by mainViewModel.navigation.collectAsState()

    // Handle navigation events triggered by the ViewModel
    LaunchedEffect(navEvent) {
        when (navEvent) {
            AuthNavEvent.ToLogin -> {
                // Navigate to Login screen
                //navController.navigate(AuthScreenRoutes.LoginScreen.route)
                navController.navigate(AuthScreenRoutes.LoginScreen.route) {
                    popUpTo(AuthScreenRoutes.ResetPasswordScreen.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
                // Reset navigation event after navigating
                mainViewModel.resetNavigation()
            }

            else -> {} // No action for other events
        }
    }


    // Screen layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .imePadding(), // Add padding when the keyboard is visible
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // App title
        Text("Notifyu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)

        // Lottie animation
        LottieAnimations(modifier = Modifier.weight(1f), R.raw.reset_password)

        // Email input field with validation
        ValidatedTextField(
            label = "Email",
            value = email,
            isError = emailError,
            errorMessage = "Please enter a valid email",
            validator = { mainViewModel.validateEmail(it) }
        )

        // Submit button to trigger password reset
        PrimaryAuthButton(
            text = "Submit",
            onClick = {
                mainViewModel.onResetPasswordClicked(email.value)
            },
            enabled = resetPasswordState !is UiState.Loading,
        )

        // Navigate to login screen if user already has an account
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Already have an account? ")
            Text(
                text = "Login",
                color = PrimaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(AuthScreenRoutes.LoginScreen.route) {
                        popUpTo(AuthScreenRoutes.ResetPasswordScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                  //  navController.navigate(AuthScreenRoutes.LoginScreen.route)
                }
            )
        }

        // UI state handling
        when (resetPasswordState) {
            is UiState.Loading -> {
                // Show progress dialog while resetting password
                AsyncProgressDialog(
                    showDialog = true,
                    message = "Sending password reset email..."
                )
            }

            is UiState.Success -> {
                // Show success message
                Toast.makeText(context, "Reset email sent successfully!", Toast.LENGTH_LONG)
                    .show()
            }

            is UiState.Error -> {
                // Show error message from UI state
                val errorMessage = (resetPasswordState as UiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }

            is UiState.Idle -> {
                // Do nothing when idle
            }
        }
    }
}
