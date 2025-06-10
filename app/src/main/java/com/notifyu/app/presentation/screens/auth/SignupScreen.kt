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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.notifyu.app.presentation.screens.auth.components.LottieAnimations
import com.notifyu.app.presentation.theme.BackgroundColor
import com.notifyu.app.presentation.theme.PrimaryColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.notifyu.app.R
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.screens.components.AsyncProgressDialog
import com.notifyu.app.presentation.screens.components.ValidatedTextField
import com.notifyu.app.presentation.viewmodel.MainViewModel
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import com.notifyu.app.presentation.viewmodel.states.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController, mainViewModel: MainViewModel) {

    val context = LocalContext.current

    // ------------------------- Input States -----------------------------

    // Email input and error state
    val email = remember { mutableStateOf("") }
    val emailError by mainViewModel.emailValidationError.collectAsState()

    // Password input, error state, and visibility toggle
    val password = remember { mutableStateOf("") }
    val passwordError by mainViewModel.passwordValidationError.collectAsState()
    val passwordVisible = remember { mutableStateOf(false) }

    // Confirm password input, error state, and visibility toggle
    val confirmPassword = remember { mutableStateOf("") }
    val confirmPasswordError by mainViewModel.confirmPasswordValidationError.collectAsState()
    val confirmPasswordVisible = remember { mutableStateOf(false) }

    // ------------------------- UI State Observers -----------------------------

    // State representing the signup process (loading, success, error)
    val signingState by mainViewModel.signingState.collectAsState()

    // Navigation state triggered from ViewModel after successful signup
    val navEvent by mainViewModel.navigation.collectAsState()

    // ------------------------- Navigation Effect -----------------------------

    // React to navigation events from ViewModel
    LaunchedEffect(navEvent) {
        when (navEvent) {
            AuthNavEvent.ToHome -> {
                navController.navigate(MainScreenRoutes.HomeScreen.route) {
                    popUpTo(AuthScreenRoutes.SignupScreen.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
             //   navController.navigate(MainScreenRoutes.HomeScreen.route)
                mainViewModel.resetNavigation()
            }

            AuthNavEvent.ToVerifyEmail -> {
                navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route) {
                    popUpTo(AuthScreenRoutes.SignupScreen.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
              //  navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)
                mainViewModel.resetNavigation()
            }

            else -> {} // Do nothing
        }
    }

    // ------------------------- UI Layout -----------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App title
        Text(
            "Notifyu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )

        // Lottie animation for signup
        LottieAnimations(modifier = Modifier.weight(1f), R.raw.signup_lottie)

        // Email input field with validation
        ValidatedTextField(
            label = "Email",
            value = email,
            isError = emailError,
            errorMessage = "Please enter a valid email",
            validator = { mainViewModel.validateEmail(it) }
        )

        // Password input field with validation
        ValidatedTextField(
            label = "Password",
            value = password,
            isError = passwordError,
            errorMessage = "Password must be 8+ chars, with 1 uppercase & 1 digit",
            validator = { mainViewModel.validatePassword(it) },
            isPassword = true,
            passwordVisible = passwordVisible
        )

        // Confirm password input field with validation
        ValidatedTextField(
            label = "Confirm Password",
            value = confirmPassword,
            isError = confirmPasswordError,
            errorMessage = "Passwords do not match",
            validator = { mainViewModel.validateConfirmPassword(password.value, it) },
            isPassword = true,
            passwordVisible = confirmPasswordVisible
        )

        // Signup button (disabled while loading)
        Button(
            onClick = {
                mainViewModel.onSignupClicked(
                    email.value,
                    password.value,
                    confirmPassword.value
                )
            },
            enabled = signingState !is UiState.Loading,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor,
                disabledContainerColor = PrimaryColor.copy(0.5f),
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Signup", color = Color.White)
        }

        // Link to login screen
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
                        popUpTo(AuthScreenRoutes.SignupScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                   // navController.navigate(AuthScreenRoutes.LoginScreen.route)
                    mainViewModel.resetNavigation()
                }
            )
        }

        // ------------------------- UI State Handling -----------------------------

        when (signingState) {
            is UiState.Loading -> {
                // Show loading dialog while account is being created
                AsyncProgressDialog(
                    showDialog = true,
                    "Account creating"
                )
            }

            is UiState.Success -> {
                // Already handled by navEvent
            }

            is UiState.Error -> {
                // Show error message using Toast
                val errorMessage = (signingState as UiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }

            is UiState.Idle -> {
                // No action required
            }
        }
    }
}
