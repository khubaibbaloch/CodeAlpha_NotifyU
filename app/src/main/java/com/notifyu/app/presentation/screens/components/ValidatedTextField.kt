package com.notifyu.app.presentation.screens.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.notifyu.app.R

@Composable
fun ValidatedTextField(
    label: String,
    value: MutableState<String>,
    isError: Boolean,
    errorMessage: String,
    validator: (String) -> Boolean,
    isPassword: Boolean = false,
    passwordVisible: MutableState<Boolean>? = null,
) {
    OutlinedTextField(
        value = value.value,
        onValueChange = {
            value.value = it
            validator(it)
        },
        label = { Text(label) },
        isError = isError,
        textStyle = TextStyle(color = Color.Black),
        modifier = Modifier.fillMaxWidth(),
        maxLines = 1,
        visualTransformation = if (isPassword && (passwordVisible?.value == false))
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword && passwordVisible != null) {
            {
                val image = if (passwordVisible.value)
                    painterResource(R.drawable.ic_visibility)
                else
                    painterResource(R.drawable.ic_visibility_off)

                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(
                        painter = image,
                        contentDescription = if (passwordVisible.value) "Hide" else "Show",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        supportingText = {
            if (isError ) {
                Text(errorMessage, color = Color.Red)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            errorTextColor = Color.Red,
            cursorColor = Color.Black,
            errorCursorColor = Color.Red,
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Gray,
            errorBorderColor = Color.Red,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Gray,
            errorLabelColor = Color.Red,
            focusedTrailingIconColor = Color.Black,
            unfocusedTrailingIconColor = Color.Gray,
            errorTrailingIconColor = Color.Red,
        )
    )
}
