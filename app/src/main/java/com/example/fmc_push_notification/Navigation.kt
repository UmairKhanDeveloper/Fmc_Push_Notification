package com.example.fmc_push_notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation(navController: NavHostController) {
    val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) {
            Screens.HomeScreen.route
        } else {
            Screens.Create_An_AccountScreen.route
        }
    ) {
        composable(Screens.Create_An_AccountScreen.route) {
            Create_An_AccountScreen(navController)
        }

        composable(Screens.LoginScreen.route) {
            LoginScreen(navController)
        }

        composable(Screens.HomeScreen.route) {
            HomeScreen(navController)
        }
    }
}


sealed class Screens(
    val route: String,
    val title: String,
    val Icon: ImageVector,
) {
    object HomeScreen : Screens(
        "HomeScreen",
        "HomeScreen",
        Icon = Icons.Filled.Add,
    )
    object LoginScreen : Screens(
        "LoginScreen",
        "LoginScreen",
        Icon = Icons.Filled.Add,
    )
    object Create_An_AccountScreen : Screens(
        "Create_An_AccountScreen",
        "Create_An_AccountScreen",
        Icon = Icons.Filled.Add,
    )
}
