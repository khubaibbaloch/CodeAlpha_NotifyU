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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.notifyu.app.presentation.screens.auth.components.LottieAnimations
import com.notifyu.app.presentation.theme.BackgroundColor
import com.notifyu.app.presentation.theme.PrimaryColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.notifyu.app.R
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.screens.auth.components.AsyncProgressDialog
import com.notifyu.app.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController, mainViewModel: MainViewModel) {
    val context = LocalContext.current
    // EMAIL VALIDATION
    val email = remember { mutableStateOf("") }
    val isEmailValid = remember(email.value) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()
    }
    val showEmailError = remember { mutableStateOf(false) }

    // PASSWORD VALIDATION
    val password = remember { mutableStateOf("") }
    val passwordPattern = "^(?=.*[A-Z])(?=.*\\d).{8,}$"
    val isPasswordValid = remember(password.value) {
        Regex(passwordPattern).matches(password.value)
    }
    val showPasswordError = remember { mutableStateOf(false) }

    // Confirm password
    val confirmPassword = remember { mutableStateOf("") }
    val isConfirmPasswordValid = remember(password.value, confirmPassword.value) {
        confirmPassword.value == password.value && confirmPassword.value.isNotEmpty()
    }
    val showConfirmPasswordError = remember { mutableStateOf(false) }

    // Password visibility toggles
    val passwordVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }

    var isCreatingAccount by remember { mutableStateOf(false) }

    val currentUser by remember { mutableStateOf(mainViewModel.auth.currentUser) }

    LaunchedEffect(true) {
        if (currentUser != null && currentUser!!.isEmailVerified) {
            navController.navigate(MainScreenRoutes.HomeScreen.route)
        } else if (currentUser != null && !currentUser!!.isEmailVerified) {
            navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)

        }
    }

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

            OutlinedTextField(
                value = email.value,
                onValueChange = {
                    email.value = it
                    showEmailError.value = it.isNotEmpty() && !isEmailValid
                },
                isError = showEmailError.value,
                label = { Text("Email") },
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

            OutlinedTextField(
                value = password.value,
                onValueChange = {
                    password.value = it
                    showPasswordError.value = it.isNotEmpty() && !isPasswordValid
                    showConfirmPasswordError.value =
                        confirmPassword.value.isNotEmpty() && confirmPassword.value != it
                },
                isError = showPasswordError.value,
                label = { Text("Password") },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible.value)
                        painterResource(R.drawable.ic_visibility)
                    else painterResource(R.drawable.ic_visibility_off)
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(
                            painter = image,
                            contentDescription = if (passwordVisible.value) "Hide password" else "Show password",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                supportingText = {
                    if (showPasswordError.value) {
                        Text(
                            "Password must be at least 8 characters, contain one uppercase letter and one digit",
                            color = Color.Red
                        )
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

            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = {
                    confirmPassword.value = it
                    showConfirmPasswordError.value = it.isNotEmpty() && it != password.value
                },
                isError = showConfirmPasswordError.value,
                label = { Text("Confirm Password") },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible.value)
                        painterResource(R.drawable.ic_visibility)
                    else painterResource(R.drawable.ic_visibility_off)
                    IconButton(onClick = {
                        confirmPasswordVisible.value = !confirmPasswordVisible.value
                    }) {
                        Icon(
                            painter = image,
                            contentDescription = if (confirmPasswordVisible.value) "Hide password" else "Show password",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                supportingText = {
                    if (showConfirmPasswordError.value) {
                        Text("Passwords do not match", color = Color.Red)
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
                    if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
                        isCreatingAccount = true
//                        mainViewModel.signUp(
//                            email = email.value,
//                            password = password.value,
//                            onResult = { isSuccess, message ->
//                                if (isSuccess) {
//                                    isCreatingAccount = false
//                                    navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)
//                                } else {
//                                    isCreatingAccount = false
//                                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
//                                }
//                            })
                        mainViewModel.authSignup(
                            email = email.value,
                            code = password.value,
                            onResult = { isSuccess, message ->
                                if (isSuccess) {
                                    isCreatingAccount = false
                                    navController.navigate(AuthScreenRoutes.VerifyEmailScreen.route)
                                } else {
                                    isCreatingAccount = false
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            })
                    } else {
                        showEmailError.value = !isEmailValid
                        showPasswordError.value = !isPasswordValid
                        showConfirmPasswordError.value = !isConfirmPasswordValid
                    }
                },
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

            if (isCreatingAccount) {
                AsyncProgressDialog(
                    showDialog = isCreatingAccount,
                    "Account creating"
                )
            }

        }
    }
}

