package br.unemat.ritmo.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val HOME = "inicio"
private const val PLAN = "plano"

@Composable
fun RitmoApp() {
    val navController = rememberNavController()
    // Shared by both destinations; returning from the plan preserves the choice.
    var selectedMinutes by rememberSaveable { mutableStateOf(25) }

    NavHost(navController = navController, startDestination = HOME) {
        composable(HOME) {
            WelcomeScreen(
                selectedMinutes = selectedMinutes,
                onSelect = { selectedMinutes = it },
                onPlan = { navController.navigate(PLAN) { launchSingleTop = true } }
            )
        }
        composable(PLAN) {
            PlanScreen(selectedMinutes = selectedMinutes, onBack = { navController.popBackStack() })
        }
    }
}
