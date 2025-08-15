package com.example.fmc_push_notification
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fmc_push_notification.chat.ChatScreen
import com.example.fmc_push_notification.model.Home
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation(navController: NavHostController) {
    val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) {
            Screens.Home.route
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

        composable(Screens.Home.route) {
            Home(navController)
        }
        composable(
            route = Screens.ChatScreen.route + "/{receiverId}/{receiverName}",
            arguments = listOf(
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("receiverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
            ChatScreen(navController, receiverId, receiverName)
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

    object Home : Screens(
        "Home",
        "Home",
        Icon = Icons.Filled.Add,
    )
    object ChatScreen : Screens(
        "ChatScreen",
        "ChatScreen",
        Icon = Icons.Filled.Add,
    )

}
