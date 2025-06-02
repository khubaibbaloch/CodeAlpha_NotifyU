package com.notifyu.app.ui.screens.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.ui.screens.events.components.CustomBasicTextField
import com.notifyu.app.ui.theme.PrimaryColor
import com.notifyu.app.R
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.viewmodel.MainViewModel
import java.security.acl.Owner

@Composable
fun EventChatScreen(navController: NavController, mainViewModel: MainViewModel) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Messages", "People", "Setting")

    val organizationOwned by mainViewModel.organizationsOwned.collectAsState()
    val organizationId by mainViewModel.onOrganizationsClick.collectAsState()
    val organizationsMemberOf by mainViewModel.organizationsMemberOf.collectAsState()
    val currentUser = mainViewModel.auth.currentUser


    val selectedOrganization = remember(organizationOwned, organizationsMemberOf, organizationId) {
        organizationOwned.find { it.id == organizationId }
            ?: organizationsMemberOf.find { it.id == organizationId }
    }

    val isOwner = remember(selectedOrganization, currentUser) {
        selectedOrganization?.owner == currentUser?.uid
    }

    val messages by mainViewModel.onOrgMessages.collectAsState()

    LaunchedEffect(messages.size) {
        mainViewModel.fetchMessagesForOrganization(organizationId)
    }

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PrimaryColor
                )
            },
            divider = { HorizontalDivider(color = Color.Gray.copy(0.2f)) }
        ) {
            tabTitles.forEachIndexed { index, title ->
                val shouldEnable = isOwner || index == 0

                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { if (shouldEnable) selectedTabIndex = index },
                    enabled = shouldEnable,
                    text = {
                        Text(
                            title,
                            color = if (selectedTabIndex == index) PrimaryColor
                            else Color.Black.copy(alpha = if (shouldEnable) 1f else 0.4f)
                        )
                    }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> EventMessagesTab(
                isOwner = isOwner,
                organization = selectedOrganization,
                messages = messages,
                mainViewModel = mainViewModel
            )

            1 -> EventPeopleTab(organization = selectedOrganization)
            2 -> EventSettingsTab(organization = selectedOrganization, currentUser = currentUser?.email.toString())
        }
    }
}

@Composable
fun EventMessagesTab(
    isOwner: Boolean,
    organization: Organization?,
    messages: List<Message>,
    mainViewModel: MainViewModel,
) {
    val textFieldValue = remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Extract messages from the organization (defaults to empty list if null)

    // Scroll to last message when message list changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState
        ) {
            items(messages.size) { index ->
                val msg = messages[index]
                MessageBubble(
                    organizationName = organization?.name ?: "",
                    message = msg.content
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isOwner) {
                CustomBasicTextField(textFieldValue)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryColor, shape = CircleShape)
                        .clickable {
                            val text = textFieldValue.value.trim()
                            if (text.isNotEmpty()) {
                                mainViewModel.addMessage(
                                    content = text,
                                    senderId = mainViewModel.auth.currentUser!!.uid
                                ) { isSu, _ ->
                                    if (isSu) {
                                        textFieldValue.value = ""
                                        // Trigger reload organization from Firestore if needed
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            } else {
                Text(
                    text = "You are not allowed to send messages.",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

        }
    }
}

@Composable
fun EventPeopleTab(organization: Organization?) {
    organization?.let {
        LazyColumn {
            items(it.members.size) { index ->
                val memberId = it.members[index] // <- member UID here
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .background(color = Color.Green, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Column {
                        Text(text = memberId, fontSize = 16.sp) // <- show member UID
                        Text(text = "Announcement", fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "Set", fontSize = 14.sp)
                }
            }
        }
    }
}


@Composable
fun EventSettingsTab(organization: Organization?, currentUser: String) {
    val orgName by remember { mutableStateOf(organization?.name ?: "") }
    val orgCode by remember { mutableStateOf(organization?.code ?: "") }

    val grayColor = Color.Gray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Top center image and edit button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
                TextButton(onClick = { /* Handle edit icon click */ }) {
                    Text(text = "Edit Icon", color = PrimaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Organization Name
        Text(text = "Organization name")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = orgName,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter organization name", color = grayColor) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Gray,
                disabledBorderColor = Color.Gray,
                disabledPlaceholderColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledTrailingIconColor = Color.Gray,
                disabledLeadingIconColor = Color.Gray,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Organization Code
        Text(text = "Organization Code")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = orgCode,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter organization code", color = grayColor) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Gray,
                disabledBorderColor = Color.Gray,
                disabledPlaceholderColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledTrailingIconColor = Color.Gray,
                disabledLeadingIconColor = Color.Gray,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Organization Owner
        Text(text = "Organization Admin")
        HorizontalDivider(color = Color.Gray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .background(color = Color.Green, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = currentUser, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = "Admin", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}



@Composable
fun MessageBubble(organizationName: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(
                Color.Gray.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(bottom = 0.dp)
    ) {
        Text(
            text = message,
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    PrimaryColor,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = organizationName,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Set",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}