package com.notifyu.app.ui.screens.chat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.ui.screens.chat.components.CustomBasicTextField
import com.notifyu.app.ui.theme.PrimaryColor
import com.notifyu.app.R
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.ui.theme.SurfaceColor
import com.notifyu.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun EventChatScreen(navController: NavController, mainViewModel: MainViewModel) {
    val context = LocalContext.current
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

    val orgEmails = remember { mutableStateListOf<String>() }
    val orgFcmTokens = remember { mutableStateListOf<String>() }
    val orgUids = remember { mutableStateListOf<String>() }


    LaunchedEffect(messages.size) {
        mainViewModel.fetchMessagesForOrganization(organizationId)
    }

    LaunchedEffect(selectedOrganization?.members) {

        val memberIds = selectedOrganization?.members ?: emptyList()
        mainViewModel.fetchUsersByIds(memberIds) { fetchedUsers ->
            Log.d("EventChatScreen", "Fetched Users: ${fetchedUsers.map { it.uid }}")
            orgEmails.clear()
            orgFcmTokens.clear()
            orgUids.clear()

            fetchedUsers.forEach { user ->
                orgEmails.add(user.email)
                orgFcmTokens.add(user.fcmToken)
                orgUids.add(user.uid)
            }

            val currentUid = currentUser?.uid
            if (currentUid != null && !isOwner && currentUid !in orgUids) {
                Log.d("EventChatScreen", "You have been removed from the organization")
                //Toast.makeText(context, "You have been removed from the organization", Toast.LENGTH_SHORT).show()
                navController.navigate(MainScreenRoutes.HomeScreen.route) {
                    popUpTo(0) // Optional: clears backstack
                }
            }
        }
    }

    val avatarList = listOf(
        R.drawable.avatar_index_0,
        R.drawable.avatar_index_1,
        R.drawable.avatar_index_2,
        R.drawable.avatar_index_3,
        R.drawable.avatar_index_4,
        R.drawable.avatar_index_5,
        R.drawable.avatar_index_6,
        R.drawable.avatar_index_7,
        R.drawable.avatar_index_8,
        R.drawable.avatar_index_9,
    )

    val avatarIndex = selectedOrganization?.avatarIndex ?: 0
    val avatarRes = avatarList.getOrElse(avatarIndex) { avatarList[0] }




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
                organizationName = selectedOrganization?.name ?: "null",
                messages = messages,
                orgFcmTokens = orgFcmTokens,
                mainViewModel = mainViewModel
            )

            1 -> EventPeopleTab(
                membersEmail = orgEmails,
                membersUid = orgUids,
                mainViewModel = mainViewModel
            )

            2 -> EventSettingsTab(
                organization = selectedOrganization,
                currentUser = currentUser?.email.toString(),
                organizationAvatar = avatarRes,
                mainViewModel = mainViewModel,
                avatarList = avatarList
            )
        }
    }
}

@Composable
fun EventMessagesTab(
    isOwner: Boolean,
    organizationName: String,
    messages: List<Message>,
    orgFcmTokens: List<String>,
    mainViewModel: MainViewModel,
) {
    val textFieldValue = remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
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
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(messages.size) { index ->
                val msg = messages[index]

                MessageBubble(
                    organizationName = organizationName,
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
                        .clip(CircleShape)
                        .background(PrimaryColor)
                        .clickable {
                            val text = textFieldValue.value.trim()
                            if (text.isNotEmpty()) {
                                textFieldValue.value = ""
                                val tempValue = textFieldValue.value
                                mainViewModel.addMessage(
                                    content = text,
                                    senderId = mainViewModel.auth.currentUser!!.uid
                                ) { isSuccess, message ->
                                    if (isSuccess) {
                                        mainViewModel.sendFcmPushNotification(
                                            context = context,
                                            targetTokens = orgFcmTokens,
                                            title = organizationName,
                                            body = tempValue
                                        )
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
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
fun EventPeopleTab(
    membersEmail: List<String>,
    membersUid: List<String>,
    mainViewModel: MainViewModel,
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        items(membersEmail.size) { index ->
            val email = membersEmail[index]
            val uid = membersUid[index]

            Row(
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .background(SurfaceColor.copy(0.5f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = email.firstOrNull()?.toString() ?: "",
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = email, fontSize = 16.sp)
                    Text(text = "Member", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    mainViewModel.removeMemberFromOrganization(uid) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = SurfaceColor.copy(0.2f)
            )
        }
    }
}


@Composable
fun EventSettingsTab(
    organization: Organization?,
    currentUser: String,
    organizationAvatar: Int,
    mainViewModel: MainViewModel,
    avatarList: List<Int>,
) {
    val orgName by remember { mutableStateOf(organization?.name ?: "") }
    val orgCode by remember { mutableStateOf(organization?.code ?: "") }
    val showBottomSheet = remember { mutableStateOf(false) }
    val grayColor = Color.Gray
    val context = LocalContext.current

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
                    painter = painterResource(organizationAvatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor.copy(0.5f))

                )
                TextButton(onClick = { showBottomSheet.value = !showBottomSheet.value }) {
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
                    .background(SurfaceColor.copy(0.5f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser.firstOrNull()?.toString() ?: "",
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = currentUser, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = "Admin", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }

    AvatarPickerBottomSheet(
        showSheet = showBottomSheet.value,
        onDismissRequest = { showBottomSheet.value = false },
        avatarList = avatarList,
        selectedAvatarIndex = organization?.avatarIndex ?: 0,
        onAvatarSelected = { selectedIndex ->
            mainViewModel.updateOrganizationAvatarIndex(
                orgId = organization?.id ?: "",
                newAvatarIndex = selectedIndex,
                onResult = { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerBottomSheet(
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    avatarList: List<Int>,
    selectedAvatarIndex: Int?, // <- new parameter to indicate selected avatar
    onAvatarSelected: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Avatar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 3
                ) {
                    avatarList.forEachIndexed { index, avatarRes ->
                        val isSelected = selectedAvatarIndex == index
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SurfaceColor.copy(0.5f))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) PrimaryColor else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    scope.launch {
                                        sheetState.hide()
                                        onAvatarSelected(index)
                                        onDismissRequest()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = avatarRes),
                                contentDescription = "Avatar $index",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}
