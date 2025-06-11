package com.notifyu.app.presentation.screens.main

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.notifyu.app.presentation.viewmodel.MainViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.screens.components.AsyncProgressDialog
import com.notifyu.app.presentation.screens.components.ValidatedTextField
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.presentation.viewmodel.states.UiState
import com.notifyu.app.utils.hideKeyboard

// Main Composable to show Create or Join Organization screen
@Composable
fun CreateJoinOrgScreen(navController: NavController, mainViewModel: MainViewModel) {

    val context = LocalContext.current // Get the current context for showing toasts or keyboard actions

    // Collecting state from ViewModel to observe the UI state and whether to create or join org
    val createJoinOrgState by mainViewModel.createJoinOrgState.collectAsState()
    val isAddOrg by mainViewModel.isAddOrg.collectAsState()

    // Local states to hold user input
    val organizationName = remember { mutableStateOf("") }
    val organizationCode = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Based on isAddOrg flag, show either Create or Join UI
        if (isAddOrg) {
            CreateOrg(
                organizationName = organizationName,
                organizationCode = organizationCode,
                onClick = {
                    // Calls ViewModel to create org when button is clicked
                    mainViewModel.authAddOrganization(
                        name = organizationName.value,
                        code = organizationCode.value,
                    )
                    hideKeyboard(context) // Hide keyboard after submission
                })
        } else {
            JoinOrg(
                organizationName = organizationName,
                organizationCode = organizationCode,
                onClick = {
                    // Calls ViewModel to join org when button is clicked
                    mainViewModel.authJoinOrganizationByNameAndCode(
                        name = organizationName.value,
                        code = organizationCode.value,
                    )
                    hideKeyboard(context) // Hide keyboard after submission
                })
        }

        // Handling different UI states returned from ViewModel
        when (createJoinOrgState) {
            is UiState.Loading -> {
                // Show loading dialog when in loading state
                AsyncProgressDialog(
                    showDialog = true,
                    "Processing..."
                )
            }

            is UiState.Success -> {
                // On success, show toast and navigate back
                val message = (createJoinOrgState as UiState.Success).data
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                // Commented navigation to Home screen, instead using popBackStack
                // navController.navigate(MainScreenRoutes.HomeScreen.route)
                navController.popBackStack()
            }

            is UiState.Error -> {
                // Show error message as a toast
                val errorMessage = (createJoinOrgState as UiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }

            is UiState.Idle -> {
                // No action needed for idle state
            }
        }
    }
}

// Composable for creating an organization
@Composable
fun CreateOrg(
    organizationName: MutableState<String>,
    organizationCode: MutableState<String>,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {

        // Text field for organization name input
        ValidatedTextField(
            label = "Organization name",
            value = organizationName,
            isError = false, // No validation currently applied
            errorMessage = "",
            validator = { false } // Always false, i.e., no error
        )

        // Text field for organization code input
        ValidatedTextField(
            label = "Organization code",
            value = organizationCode,
            isError = false,
            errorMessage = "",
            validator = { false }
        )

        // Submit button for creating organization
        Button(
            onClick = {
                // Check if inputs are empty
                if (organizationName.value.isBlank() || organizationCode.value.isBlank()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                } else {
                    onClick() // Call the passed lambda if inputs are valid
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor) // PrimaryColor should be defined elsewhere
        ) {
            Text(text = "Create organization", color = Color.White)
        }
    }
}

// Composable for joining an organization
@Composable
fun JoinOrg(
    organizationName: MutableState<String>,
    organizationCode: MutableState<String>,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {

        // Text field for organization name input
        ValidatedTextField(
            label = "Organization name",
            value = organizationName,
            isError = false,
            errorMessage = "",
            validator = { false } // No actual validation logic
        )

        // Text field for organization code input
        ValidatedTextField(
            label = "Organization code",
            value = organizationCode,
            isError = false,
            errorMessage = "",
            validator = { false }
        )

        // Submit button for joining organization
        Button(
            onClick = {
                // Basic check for empty fields
                if (organizationName.value.isBlank() || organizationCode.value.isBlank()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                } else {
                    onClick() // Call the passed lambda if valid
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text(text = "Join organization", color = Color.White)
        }
    }
}
