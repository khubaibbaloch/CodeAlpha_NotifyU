package com.notifyu.app.presentation.screens.auth

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.presentation.screens.auth.components.LottieAnimations
import com.notifyu.app.presentation.theme.BackgroundColor
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.R
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.screens.auth.components.PrimaryAuthButton
import com.notifyu.app.presentation.viewmodel.MainViewModel
import com.notifyu.app.presentation.screens.components.AsyncProgressDialog
import com.notifyu.app.presentation.screens.components.ValidatedTextField
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import com.notifyu.app.presentation.viewmodel.states.UiState

@Composable
fun LoginScreen(navController: NavController, mainViewModel: MainViewModel) {
    val context = LocalContext.current

    // Input field states
    val email = remember { mutableStateOf("") }  // Email input
    val emailError by mainViewModel.emailValidationError.collectAsState()  // Email validation error from ViewModel

    val password = remember { mutableStateOf("") }  // Password input
    val passwordError by mainViewModel.passwordValidationError.collectAsState()  // Password validation error (not used here)
    val passwordVisible = remember { mutableStateOf(false) }  // Toggle for showing/hiding password

    // UI state collection
    val loginState by mainViewModel.loginState.collectAsState()  // Login process state
    val navEvent by mainViewModel.navigation.collectAsState()    // Navigation event state
    val currentUser by mainViewModel.currentUser.collectAsState() // Firebase current user state

    // Handle navigation when navEvent changes
    LaunchedEffect(navEvent) {
        when (navEvent) {
            AuthNavEvent.ToHome -> {
                // Navigate to home screen and clear login screen from backstack
                navController.navigate(MainScreenRoutes.HomeScreen.route) {
                    popUpTo(AuthScreenRoutes.LoginScreen.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
             //   hideKeyboard(context)

                mainViewModel.resetNavigation()
            }

            AuthNavEvent.ToVerifyEmail -> {
                // Navigate to email verification screen
                navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route) {
                    popUpTo(AuthScreenRoutes.LoginScreen.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
                //navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)
                mainViewModel.resetNavigation()
            }

            else -> {}
        }
    }


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
        Text("Notifyu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)

        // Login illustration animation
        LottieAnimations(modifier = Modifier.weight(1f), R.raw.login_lottie)

        // Email input with validation
        ValidatedTextField(
            label = "Email",
            value = email,
            isError = emailError,
            errorMessage = "Please enter a valid email",
            validator = { mainViewModel.validateEmail(it) }
        )

        // Password input with toggle visibility
        ValidatedTextField(
            label = "Password",
            value = password,
            isError = false, // Password error is not shown
            errorMessage = "",
            validator = { false },  // No validation for password
            isPassword = true,
            passwordVisible = passwordVisible
        )

        // "Forget Password" clickable text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Forget Password",
                color = PrimaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(AuthScreenRoutes.ResetPasswordScreen.route) {
                        popUpTo(AuthScreenRoutes.LoginScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                   // navController.navigate(AuthScreenRoutes.ResetPasswordScreen.route)
                    mainViewModel.resetNavigation()
                }
            )
        }

        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        // Login Button
//            Button(
//                onClick = {
//                    mainViewModel.onLoginClicked(
//                        email.value,
//                        password.value,
//                        currentUser = currentUser
//                    )
//                },
//                enabled = loginState !is UiState.Loading,
//                shape = RoundedCornerShape(4.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Login")
//            }

        PrimaryAuthButton(
            text = "Login",
            onClick = {
                mainViewModel.onLoginClicked(
                    email.value,
                    password.value,
                    currentUser = currentUser
                )
                //hideKeyboard(context)
            },
            enabled = loginState !is UiState.Loading,
        )

        // Navigate to signup screen if no account
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Don't have an account? ")
            Text(
                text = "Signup",
                color = PrimaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(AuthScreenRoutes.SignupScreen.route)
                    mainViewModel.resetNavigation()
                }
            )
        }

        // Handle UI feedback based on login state
        when (loginState) {
            is UiState.Loading -> {
                // Show loading dialog
                AsyncProgressDialog(
                    showDialog = true,
                    message = "Authenticating account..."
                )
            }

            is UiState.Success -> {
                // No specific action needed here; navigation handles success
            }

            is UiState.Error -> {
                // Show error as toast message
                val errorMessage = (loginState as UiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }

            is UiState.Idle -> {
                // Do nothing
            }
        }
    }

}
