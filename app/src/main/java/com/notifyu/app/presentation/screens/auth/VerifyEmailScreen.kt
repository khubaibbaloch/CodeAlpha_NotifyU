package com.notifyu.app.presentation.screens.auth


import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.notifyu.app.R
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.screens.components.AsyncProgressDialog
import com.notifyu.app.presentation.screens.auth.components.LottieAnimations
import com.notifyu.app.presentation.screens.auth.components.PrimaryAuthButton
import com.notifyu.app.presentation.screens.components.ValidatedTextField
import com.notifyu.app.presentation.theme.BackgroundColor
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import com.notifyu.app.presentation.viewmodel.states.UiState
import com.notifyu.app.utils.hideKeyboard

@Composable
fun VerifyEmailScreen(navController: NavController, mainViewModel: MainViewModel) {

    // Context and lifecycle references
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Email state and validation error
    val email = remember { mutableStateOf("") }
    val emailError by mainViewModel.emailValidationError.collectAsState()

    // UI and auth states
    val emailVerificationState by mainViewModel.emailVerificationState.collectAsState()
    val navEvent by mainViewModel.navigation.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()

    // Handle navigation events (e.g., move to Home screen after verification)
    LaunchedEffect(navEvent) {
        when (navEvent) {
            is AuthNavEvent.ToHome -> {
                navController.navigate(MainScreenRoutes.HomeScreen.route) {
                    popUpTo(0) { inclusive = true } // Clears entire back stack
                    launchSingleTop = true
                }
                mainViewModel.resetNavigation()
            }

            else -> {}
        }
    }

    // Automatically check if the email has been verified when returning to this screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel.authCheckEmailVerification()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Main UI scaffold
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

        // Lottie animation
        LottieAnimations(modifier = Modifier.weight(1f), R.raw.verification_email_lottie)

        // Email input field with validation
        ValidatedTextField(
            label = "Email",
            value = email,
            isError = emailError,
            errorMessage = "Please enter a valid email",
            validator = { mainViewModel.validateEmail(it) }
        )

        // Submit button to initiate email verification
//            Button(
//                onClick = {
//                    mainViewModel.onVerifyEmailClicked(email.value, currentUser = currentUser)
//                },
//                enabled = emailVerificationState !is UiState.Loading,
//                shape = RoundedCornerShape(4.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Submit")
//            }

        PrimaryAuthButton(
            text = "Submit",
            onClick = {
                mainViewModel.onVerifyEmailClicked(email.value, currentUser = currentUser)
                hideKeyboard(context)
            },
            enabled = emailVerificationState !is UiState.Loading
        )


        // Link to login screen for existing users
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
                        popUpTo(AuthScreenRoutes.VerifyEmailScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                   // navController.navigate(AuthScreenRoutes.LoginScreen.route)
                    mainViewModel.resetNavigation()
                }
            )
        }

        // Handle different UI states
        when (emailVerificationState) {
            is UiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncProgressDialog(
                        showDialog = true,
                        message = "A verification email has been sent to your email address."
                    )
                }
            }


            is UiState.Success -> {
                // Optionally show success message if needed
                 val message = (emailVerificationState as UiState.Success).data
                 Toast.makeText(context, message , Toast.LENGTH_SHORT).show()
            }

            is UiState.Error -> {
                val message = (emailVerificationState as UiState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            is UiState.Idle -> {
                // No action needed in idle state
            }
        }
    }

}
