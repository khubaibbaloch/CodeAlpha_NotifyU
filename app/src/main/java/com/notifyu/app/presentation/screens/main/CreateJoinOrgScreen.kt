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

@Composable
fun CreateJoinOrgScreen(navController: NavController, mainViewModel: MainViewModel) {

    val context = LocalContext.current
    //  UI STATES
    val createJoinOrgState by mainViewModel.createJoinOrgState.collectAsState()
    val isAddOrg by mainViewModel.isAddOrg.collectAsState()

    val organizationName = remember { mutableStateOf("") }
    val organizationCode = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        if (isAddOrg) {
            CreateOrg(
                organizationName = organizationName,
                organizationCode = organizationCode,
                onClick = {
                    mainViewModel.authAddOrganization(
                        name = organizationName.value,
                        code = organizationCode.value,
                    )
                    hideKeyboard(context)
                })
        } else {
            JoinOrg(
                organizationName = organizationName,
                organizationCode = organizationCode,
                onClick = {
                    mainViewModel.authJoinOrganizationByNameAndCode(
                        name = organizationName.value,
                        code = organizationCode.value,
                    )
                    hideKeyboard(context)
                })
        }

        when (createJoinOrgState) {
            is UiState.Loading -> {
                AsyncProgressDialog(
                    showDialog = true,
                    "Processing..."
                )
            }

            is UiState.Success -> {
                val message = (createJoinOrgState as UiState.Success).data
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
               // navController.navigate(MainScreenRoutes.HomeScreen.route)
                navController.popBackStack()
            }

            is UiState.Error -> {
                val errorMessage = (createJoinOrgState as UiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

            }

            is UiState.Idle -> {

            }
        }
    }
}

@Composable
fun CreateOrg(
    organizationName: MutableState<String>,
    organizationCode: MutableState<String>,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        ValidatedTextField(
            label = "Organization name",
            value = organizationName,
            isError = false,
            errorMessage = "",
            validator = { false }
        )

        ValidatedTextField(
            label = "Organization code",
            value = organizationCode,
            isError = false,
            errorMessage = "",
            validator = { false }
        )
        Button(
            onClick = {
                if (organizationName.value.isBlank() || organizationCode.value.isBlank()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                } else {
                    onClick()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text(text = "Create organization", color = Color.White)
        }
    }
}

@Composable
fun JoinOrg(
    organizationName: MutableState<String>,
    organizationCode: MutableState<String>,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        ValidatedTextField(
            label = "Organization name",
            value = organizationName,
            isError = false,
            errorMessage = "",
            validator = { false }
        )

        ValidatedTextField(
            label = "Organization code",
            value = organizationCode,
            isError = false,
            errorMessage = "",
            validator = { false }
        )


        Button(
            onClick = {
                if (organizationName.value.isBlank() || organizationCode.value.isBlank()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                } else {
                    onClick()
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
