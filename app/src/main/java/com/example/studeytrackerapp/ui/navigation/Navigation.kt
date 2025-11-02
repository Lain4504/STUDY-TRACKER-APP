package com.example.studeytrackerapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.studeytrackerapp.ui.screens.AddSessionScreen
import com.example.studeytrackerapp.ui.screens.HomeScreen
import com.example.studeytrackerapp.ui.viewmodel.AddSessionViewModel
import com.example.studeytrackerapp.ui.viewmodel.HomeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddSession : Screen("add_session")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    addSessionViewModel: AddSessionViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onAddSessionClick = {
                    navController.navigate(Screen.AddSession.route)
                }
            )
        }
        
        composable(Screen.AddSession.route) {
            AddSessionScreen(
                viewModel = addSessionViewModel,
                onBack = {
                    navController.popBackStack()
                    homeViewModel.refresh()
                }
            )
        }
    }
}

