package it.meteoapp.clone.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.meteoapp.clone.data.repository.ForecastResult
import it.meteoapp.clone.ui.detail.DetailScreen
import it.meteoapp.clone.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home   : Screen("home")
    object Detail : Screen("detail")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Stato condiviso tra Home e Detail (passaggio dati senza serializzazione complessa)
    var sharedForecast by remember { mutableStateOf<ForecastResult?>(null) }
    var sharedDayIndex by remember { mutableStateOf(0) }

    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onDayClick = { result, dayIndex ->
                    sharedForecast = result
                    sharedDayIndex = dayIndex
                    navController.navigate(Screen.Detail.route)
                }
            )
        }

        composable(Screen.Detail.route) {
            val forecast = sharedForecast ?: return@composable
            DetailScreen(
                forecastResult  = forecast,
                initialDayIndex = sharedDayIndex,
                onBack          = { navController.popBackStack() }
            )
        }
    }
}
