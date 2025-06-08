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
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.notifyu.app.R
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.screens.components.AsyncProgressDialog
import com.notifyu.app.presentation.screens.auth.components.LottieAnimations
import com.notifyu.app.presentation.screens.components.ValidatedTextField
import com.notifyu.app.presentation.theme.BackgroundColor
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import com.notifyu.app.presentation.viewmodel.states.UiState

@Composable
fun VerifyEmailScreen(navController: NavController, mainViewModel: MainViewModel) {
    // AFTER MVVM
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val email = remember { mutableStateOf("") }
    val emailError by mainViewModel.emailValidationError.collectAsState()

    //  UI STATES
    val emailVerificationState by mainViewModel.emailVerificationState.collectAsState()
    val navEvent by mainViewModel.navigation.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()


    LaunchedEffect(navEvent) {
        when (navEvent) {
            is AuthNavEvent.ToHome -> {
                navController.navigate(MainScreenRoutes.HomeScreen.route)
                mainViewModel.resetNavigation()
            }

            else -> {}
        }
    }

    // On app resume or return to this screen
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
            LottieAnimations(modifier = Modifier.weight(1f), R.raw.verification_email_lottie)
            ValidatedTextField(
                label = "Email",
                value = email,
                isError = emailError,
                errorMessage = "Please enter a valid email",
                validator = { mainViewModel.validateEmail(it) }
            )

            Button(
                onClick = {
                    mainViewModel.onVerifyEmailClicked(email.value, currentUser = currentUser)
                },
                enabled = emailVerificationState !is UiState.Loading,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
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
                        // Navigate to login screen here
                        navController.navigate(AuthScreenRoutes.LoginScreen.route)
                    }
                )
            }

            when (emailVerificationState) {
                is UiState.Loading -> {
                    AsyncProgressDialog(
                        showDialog = true,
                        message = "Waiting for email verification..."
                    )
                }

                is UiState.Success -> {
//                    val message = (emailVerificationState as UiState.Success).data
//                    Toast.makeText(context, message , Toast.LENGTH_SHORT).show()
                }

                is UiState.Error -> {
                    val message = (emailVerificationState as UiState.Error).message
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }

        }
    }
}
