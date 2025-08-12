package com.example.fmc_push_notification

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fmc_push_notification.firebase.ResultState
import com.example.fmc_push_notification.firebase_realtimedatabase.RealTimeDbRepository
import com.example.fmc_push_notification.firebase_realtimedatabase.RealTimeUser
import com.example.fmc_push_notification.firebase_realtimedatabase.RealTimeViewModel
import com.example.fmc_push_notification.firebase_realtimedatabase.RealTimeViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val firebaseDb = FirebaseDatabase.getInstance().reference
    val repo = remember { RealTimeDbRepository(firebaseDb, context) }
    val viewModel: RealTimeViewModel = viewModel(factory = RealTimeViewModelFactory(repo))

    val state by viewModel.res
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(state.item) {
        if (state.item.isNotEmpty()) {
            listState.animateScrollToItem(state.item.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat en Tiempo Real", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colors.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            MessageInput(
                text = text,
                onTextChanged = { text = it },
                onSendClick = {
                    if (text.isNotBlank()) {
                        scope.launch {
                            viewModel.insert(
                                RealTimeUser.RealTimeItems(text = text)
                            ).collect { result ->
                                when (result) {
                                    is ResultState.Success -> text = ""
                                    is ResultState.Error -> Log.e("HomeScreen", "Error: ${result.error.message}")
                                    ResultState.Loading -> {}
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error.isNotEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.error}", color = Color.Red)
                    }
                }
                state.item.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aún no hay mensajes. ¡Sé el primero en enviar uno!")
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(state.item) { user ->
                            MessageBubble(
                                message = user.items.text,
                                isOwnMessage = false // TODO: hook this to your user ID logic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: String, isOwnMessage: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomEnd = if (isOwnMessage) 0.dp else 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 0.dp
                    )
                )
                .background(
                    if (isOwnMessage) MaterialTheme.colors.primary
                    else MaterialTheme.colors.secondary.copy(alpha = 0.2f)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message,
                fontSize = 16.sp,
                color = if (isOwnMessage) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun MessageInput(text: String, onTextChanged: (String) -> Unit, onSendClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            placeholder = { Text("Escribe un mensaje...") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSendClick,
            enabled = text.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colors.primary, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Enviar mensaje",
                tint = Color.White
            )
        }
    }
}
