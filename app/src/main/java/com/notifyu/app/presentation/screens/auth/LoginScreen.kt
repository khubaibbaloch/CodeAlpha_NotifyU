package com.notifyu.app.presentation.screens.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.notifyu.app.presentation.screens.auth.components.LottieAnimations
import com.notifyu.app.presentation.theme.BackgroundColor
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.R
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.viewmodel.MainViewModel
import androidx.compose.runtime.*
import com.notifyu.app.presentation.screens.components.AsyncProgressDialog
import com.notifyu.app.presentation.screens.components.ValidatedTextField
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import com.notifyu.app.presentation.viewmodel.states.UiState

@Composable
fun LoginScreen(navController: NavController, mainViewModel: MainViewModel) {
    // AFTER  MVVM
    val context = LocalContext.current

    // Email Validation
    val email = remember { mutableStateOf("") }
    val emailError by mainViewModel.emailValidationError.collectAsState()

    // Password Validation
    val password = remember { mutableStateOf("") }
    val passwordError by mainViewModel.passwordValidationError.collectAsState()
    val passwordVisible = remember { mutableStateOf(false) }


    //  UI Sates
    val loginState by mainViewModel.loginState.collectAsState()
    val navEvent by mainViewModel.navigation.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()



    LaunchedEffect(navEvent) {
        when (navEvent) {
            AuthNavEvent.ToHome -> {
                navController.navigate(MainScreenRoutes.HomeScreen.route) {
                    popUpTo(AuthScreenRoutes.LoginScreen.route) { inclusive = true }
                }
                mainViewModel.resetNavigation()
            }
            AuthNavEvent.ToVerifyEmail -> {
                navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)
                mainViewModel.resetNavigation()
            }
            else -> {}
        }
    }



    Scaffold(containerColor = BackgroundColor) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Notifyu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
            LottieAnimations(modifier = Modifier.weight(1f), R.raw.login_lottie)

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
                isError = false,
                errorMessage = "",
                validator = {false },
                isPassword = true,
                passwordVisible = passwordVisible
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Forget Password",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(AuthScreenRoutes.ResetPasswordScreen.route)
                    }
                )
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Button(
                onClick = {
                    mainViewModel.onLoginClicked(email.value,password.value, currentUser =currentUser )
                },
                enabled = loginState !is UiState.Loading,
                shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
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
                        // Navigate to login screen here
                        navController.navigate(AuthScreenRoutes.SignupScreen.route)
                    }
                )
            }

            when(loginState){
                is UiState.Loading -> {
                    AsyncProgressDialog(
                        showDialog = true,
                        message = "Authenticating account..."
                    )
                }
                is UiState.Success -> {

                }
                is UiState.Error -> {
                    val errorMessage = (loginState as UiState.Error).message
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
                is UiState.Idle -> {}

            }

        }
    }
}