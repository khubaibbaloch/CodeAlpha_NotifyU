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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import com.notifyu.app.presentation.screens.auth.components.AsyncProgressDialog
import com.notifyu.app.presentation.screens.auth.components.LottieAnimations
import com.notifyu.app.presentation.theme.BackgroundColor
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun VerifyEmailScreen(navController: NavController, mainViewModel: MainViewModel) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // EMAIL VALIDATION
    val email = remember { mutableStateOf("") }
    val isEmailValid = remember(email.value) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()
    }
    val showEmailError = remember { mutableStateOf(false) }

    val currentUser by remember { mutableStateOf(mainViewModel.auth.currentUser) }

    var isVerifyingEmail by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        currentUser?.let { user ->
            if (user.isEmailVerified) {
                navController.navigate(MainScreenRoutes.HomeScreen.route)
            }
        }

    }



    // On app resume or return to this screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel.authCheckEmailVerification { isSuccess ->
                    if (isSuccess) {
                        isVerifyingEmail = false
                        navController.navigate(MainScreenRoutes.HomeScreen.route)
                    } else {
                        Toast.makeText(context, "email is not verified", Toast.LENGTH_SHORT).show()
                    }
                }
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
            OutlinedTextField(
                value = email.value,
                onValueChange = {
                    email.value = it
                    showEmailError.value = it.isNotEmpty() && !isEmailValid
                },
                isError = showEmailError.value,
                label = { Text("Email Verification") },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (showEmailError.value) {
                        Text("Please enter a valid email address", color = Color.Red)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.Red,  // Set error text color to red

                    cursorColor = Color.Black,
                    errorCursorColor = Color.Red,

                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red,

                    focusedLeadingIconColor = Color.Black,
                    unfocusedLeadingIconColor = Color.Gray,
                    errorLeadingIconColor = Color.Red,

                    focusedTrailingIconColor = Color.Black,
                    unfocusedTrailingIconColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray,
                    errorTrailingIconColor = Color.Red,

                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    errorLabelColor = Color.Red,

                    )
            )
            Button(
                onClick = {
                    isVerifyingEmail = true
                    currentUser?.let {
//                        mainViewModel.sendEmailVerification { isSuccess, message ->
//                            email.value = ""
//                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                            if (!isSuccess) {
//                                isVerifyingEmail = false
//                            }
//                        }
                        mainViewModel.authSendEmailVerification { isSuccess, message ->
                            email.value = ""
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (!isSuccess) {
                                isVerifyingEmail = false
                            }
                        }
                    } ?: run {
                        isVerifyingEmail = false
                        Toast.makeText(context, "No user found", Toast.LENGTH_SHORT).show()
                    }
                },
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

            if (isVerifyingEmail) {
                AsyncProgressDialog(
                    showDialog = isVerifyingEmail,
                    message = "Waiting for email verification..."
                )
            }


        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountVerifyingDialog(isCreatingAccount: Boolean, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    // Trigger dialog when account creation starts
    LaunchedEffect(isCreatingAccount) {
        if (isCreatingAccount) {
            showDialog = true
            delay(5000) // Simulate account creation time
            showDialog = false
            navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)
        }
    }

    if (showDialog) {
        BasicAlertDialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Creating account...")
                }
            }
        }
    }
}
