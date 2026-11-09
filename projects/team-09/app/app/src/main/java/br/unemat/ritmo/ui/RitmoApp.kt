package br.unemat.ritmo.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.unemat.ritmo.data.HistorySummary
import br.unemat.ritmo.data.SessionRepository
import br.unemat.ritmo.model.SessionRules
import kotlinx.coroutines.launch

private const val HOME = "inicio"
private const val PLAN = "plano"

@Composable
fun RitmoApp(repository: SessionRepository) {
    val navController = rememberNavController()
    // Shared by both destinations; returning from the plan preserves the choice.
    var selectedMinutes by rememberSaveable { mutableStateOf(25) }
    // Form state (Sprint 04): the text fields only show what is stored here.
    var subject by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    // Persistent data (Sprint 05): read from Room; the UI updates whenever the table changes.
    val sessions by repository.recentSessions.collectAsState(initial = emptyList())
    val summary by repository.summary.collectAsState(initial = HistorySummary(0, 0))
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                },
                summary = summary,
                sessions = sessions,
                snackbarHostState = snackbarHostState
            )
        }
        composable(PLAN) {
            PlanScreen(
                selectedMinutes = selectedMinutes,
                subject = subject.trim(),
                goal = goal.trim(),
                onBack = { navController.popBackStack() },
                onSave = {
                    navController.popBackStack()
                    // Room runs the insert off the main thread (suspend function).
                    scope.launch {
                        repository.save(subject, goal, selectedMinutes)
                        // Saved: clear the form for the next session and confirm.
                        subject = ""
                        goal = ""
                        showErrors = false
                        snackbarHostState.showSnackbar("Sessão salva no histórico.")
                    }
                }
            )
        }
    }
}
