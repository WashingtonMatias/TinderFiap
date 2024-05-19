package com.apiguave.tinderclonecompose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apiguave.tinderclonecompose.home.HomeScreen
import com.apiguave.tinderclonecompose.login.LoginOn
import com.apiguave.tinderclonecompose.login.LoginScreen
import com.apiguave.tinderclonecompose.signup.SignUpScreen

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "loginScreen") {
        composable("loginScreen") {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signUpScreen") },
                onNavigateToHome = { navController.navigate("HomeScreen") },
                onNavigateToLoginOn = { navController.navigate("loginOnScreen") }
            )
        }
        composable("loginOnScreen") {
            LoginOn(
                onNavigateToSignUp = { navController.navigate("signUpScreen") },
                onLoginSuccess = { navController.navigate("homeScreen") }
            )
        }
        composable("signUpScreen") {
            SignUpScreen(
                onLoginSuccess = { navController.navigate("homeScreen") }
            )
        }
        composable("homeScreen") {
            HomeScreen(
                navigateToEditProfile = { navController.navigate("editProfileScreen") },
                navigateToMatchList = { navController.navigate("matchListScreen") }
            )
        }
    }
}