package br.unemat.ritmo.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.unemat.ritmo.model.SessionRules

private const val HOME = "inicio"
private const val PLAN = "plano"

@Composable
fun RitmoApp() {
    val navController = rememberNavController()
    // Shared by both destinations; returning from the plan preserves the choice.
    var selectedMinutes by rememberSaveable { mutableStateOf(25) }
    // Form state (Sprint 04): the text fields only show what is stored here.
    var subject by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = HOME) {
        composable(HOME) {
            WelcomeScreen(
                selectedMinutes = selectedMinutes,
                onSelect = { selectedMinutes = it },
                subject = subject,
                goal = goal,
                showErrors = showErrors,
                onSubjectChange = { subject = it },
                onGoalChange = { goal = it },
                onPlan = {
                    // Invalid input: show the messages and stay on this screen.
                    if (SessionRules.isValid(subject, goal)) {
                        navController.navigate(PLAN) { launchSingleTop = true }
                    } else {
                        showErrors = true
                    }
                }
            )
        }
        composable(PLAN) {
            PlanScreen(
                selectedMinutes = selectedMinutes,
                subject = subject.trim(),
                goal = goal.trim(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
