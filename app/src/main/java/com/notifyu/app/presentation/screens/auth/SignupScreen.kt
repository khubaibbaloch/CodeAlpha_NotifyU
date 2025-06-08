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

    // EMAIL VALIDATION
    val email = remember { mutableStateOf("") }
    val emailError by mainViewModel.emailValidationError.collectAsState()

    // PASSWORD VALIDATION
    val password = remember { mutableStateOf("") }
    val passwordError by mainViewModel.passwordValidationError.collectAsState()
    val passwordVisible = remember { mutableStateOf(false) }

    // CONFIRM PASSWORD VALIDATION
    val confirmPassword = remember { mutableStateOf("") }
    val confirmPasswordError by mainViewModel.confirmPasswordValidationError.collectAsState()
    val confirmPasswordVisible = remember { mutableStateOf(false) }

    //  UI STATES
    val signingState by mainViewModel.signingState.collectAsState()
    val navEvent by mainViewModel.navigation.collectAsState()


    LaunchedEffect(navEvent) {
        when (navEvent) {
            AuthNavEvent.ToHome -> {
                navController.navigate(MainScreenRoutes.HomeScreen.route)
                mainViewModel.resetNavigation()

            }
            AuthNavEvent.ToVerifyEmail -> {
                navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)
                mainViewModel.resetNavigation()

            }
            else -> {}
        }
    }
//
//    LaunchedEffect(uiMessage) {
//        if (uiMessage.isNotEmpty()) {
//            Toast.makeText(context, uiMessage, Toast.LENGTH_SHORT).show()
//            mainViewModel.clearUiMessage()
//        }
//    }




    Scaffold(containerColor = BackgroundColor) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Notifyu",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )

            LottieAnimations(modifier = Modifier.weight(1f), R.raw.signup_lottie)

            ValidatedTextField(
                label = "Email",
                value = email,
                isError = emailError,
                errorMessage = "Please enter a valid email",
                validator = { mainViewModel.validateEmail(it) }
            )


            ValidatedTextField(
                label = "Password",
                value = password,
                isError = passwordError,
                errorMessage = "Password must be 8+ chars, with 1 uppercase & 1 digit",
                validator = { mainViewModel.validatePassword(it) },
                isPassword = true,
                passwordVisible = passwordVisible
            )


            ValidatedTextField(
                label = "Confirm Password",
                value = confirmPassword,
                isError = confirmPasswordError,
                errorMessage = "Passwords do not match",
                validator = { mainViewModel.validateConfirmPassword(password.value, it) },
                isPassword = true,
                passwordVisible = confirmPasswordVisible
            )

            Button(
                onClick = {
                    mainViewModel.onSignupClicked(email.value,password.value,confirmPassword.value)
                },
                enabled = signingState !is UiState.Loading,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Signup")
            }


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
                        navController.navigate(AuthScreenRoutes.LoginScreen.route)
                    }
                )
            }

            when (signingState) {
                is UiState.Loading -> {
                    AsyncProgressDialog(
                        showDialog = true,
                        "Account creating"
                    )
                }

                is UiState.Success -> {
                    //navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)
                }

                is UiState.Error -> {
                    val errorMessage = (signingState as UiState.Error).message
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

                }

                is UiState.Idle -> {

                }
            }
        }
    }
}

