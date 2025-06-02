package com.notifyu.app.ui.screens.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifyu.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventChatScreenTopBar(title: String) {
    TopAppBar(
        navigationIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(20.dp),
                tint = Color.Black
            )
        },
        title = {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        },
        actions = {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(20.dp),
                tint = Color.Black
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),


    )
}