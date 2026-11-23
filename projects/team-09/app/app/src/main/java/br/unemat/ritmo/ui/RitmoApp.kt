package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.unemat.ritmo.data.GoalStore
import br.unemat.ritmo.data.HistorySummary
import br.unemat.ritmo.data.SessionRepository
import br.unemat.ritmo.model.SessionRules
import br.unemat.ritmo.model.WeeklyGoal
import br.unemat.ritmo.timer.SessionClock
import kotlinx.coroutines.launch

const val TAB_PLAN = "planejar"
const val TAB_TIMER = "cronometro"
const val TAB_HISTORY = "historico"

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(TAB_PLAN, "Planejar", Icons.Filled.Edit),
    Tab(TAB_TIMER, "Cronômetro", Icons.Filled.PlayArrow),
    Tab(TAB_HISTORY, "Histórico", Icons.AutoMirrored.Filled.List),
)

@Composable
fun RitmoApp(repository: SessionRepository, requestedTab: String? = null, onTabOpened: () -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current
    // Sprint 06: the student types the duration; it is kept as text while being edited.
    var minutesText by rememberSaveable { mutableStateOf(SessionRules.MINUTES_DEFAULT.toString()) }
    // Form state (Sprint 04): the text fields only show what is stored here.
    var subject by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var confirmReplace by rememberSaveable { mutableStateOf(false) }

    // Sprint 07: a sessão e o cronômetro vivem fora da tela (SessionClock), então continuam ao trocar de aba ou fechar o app.
    val session by SessionClock.state.collectAsState()

    // Persistent data (Sprint 05): read from Room; the UI updates whenever the table changes.
    val sessions by repository.sessions.collectAsState(initial = emptyList())
    val summary by repository.summary.collectAsState(initial = HistorySummary(0, 0))
    // Meta da semana. Sprint 07: recalculado a cada recomposição (cálculo barato), para a semana virar
    // na segunda-feira mesmo com o app aberto; a consulta ao banco só é refeita quando o valor muda.
    val weekStart = WeeklyGoal.startOfWeek(System.currentTimeMillis())
    val weekMinutes by remember(weekStart) { repository.minutesSince(weekStart) }.collectAsState(initial = 0)
    val goalStore = remember { GoalStore(context) }
    var weeklyGoal by remember { mutableIntStateOf(goalStore.get()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    /** [restore] = false abre a aba do topo (sessão nova ou recém-salva), sem a rolagem da última visita. */
    fun openTab(route: String, restore: Boolean = true) {
        navController.navigate(route) {
            // Abas: voltar sempre leva a Planejar, e cada aba guarda a própria rolagem.
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = restore
        }
    }
    LaunchedEffect(requestedTab) {
        if (requestedTab != null) {
            openTab(requestedTab)
            onTabOpened()
        }
    }

    fun planSession() {
        SessionClock.plan(context, subject, goal, minutesText.trim().toInt())
        openTab(TAB_TIMER, restore = false)
    }

    Scaffold(
        containerColor = Paper,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFE8ECE2)) {
                TABS.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = { openTab(tab.route) },
                        icon = {
                            BadgedBox(badge = { if (tab.route == TAB_TIMER && session?.timer?.running == true) Badge(containerColor = Forest) }) {
                                Icon(tab.icon, contentDescription = null)
                            }
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Forest, selectedTextColor = Forest, indicatorColor = Lime,
                            unselectedIconColor = Muted, unselectedTextColor = Muted,
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = TAB_PLAN, modifier = Modifier.padding(padding)) {
            composable(TAB_PLAN) {
                WelcomeScreen(
                    minutesText = minutesText,
                    onMinutesChange = { minutesText = it },
                    subject = subject,
                    goal = goal,
                    showErrors = showErrors,
                    onSubjectChange = { subject = it },
                    onGoalChange = { goal = it },
                    onPlan = {
                        // Invalid input: show the messages and stay on this screen.
                        if (!SessionRules.isValid(subject, goal, minutesText)) {
                            showErrors = true
                        } else {
                            val timer = session?.timer
                            val now = System.currentTimeMillis()
                            // Uma sessão já começada só é trocada depois de confirmar.
                            if (timer != null && timer.started(now) && !timer.finished(now)) confirmReplace = true
                            else planSession()
                        }
                    },
                )
            }
            composable(TAB_TIMER) {
                PlanScreen(
                    session = session,
                    onSave = { saved ->
                        SessionClock.clear(context)
                        openTab(TAB_HISTORY, restore = false)
                        // Room runs the insert off the main thread (suspend function).
                        scope.launch {
                            repository.save(saved.subject, saved.goal, saved.minutes)
                            // Saved: clear the form for the next session and confirm.
                            subject = ""
                            goal = ""
                            showErrors = false
                            snackbarHostState.showSnackbar("Sessão salva no histórico.")
                        }
                    },
                    onAdjust = { openTab(TAB_PLAN) },
                )
            }
            composable(TAB_HISTORY) {
                HistoryScreen(
                    summary = summary,
                    sessions = sessions,
                    weekMinutes = weekMinutes,
                    weeklyGoal = weeklyGoal,
                    onWeeklyGoalChange = { weeklyGoal = it; goalStore.set(it) },
                    onDelete = { deleted ->
                        scope.launch {
                            repository.delete(deleted)
                            val result = snackbarHostState.showSnackbar(
                                "Sessão excluída.", actionLabel = "Desfazer", duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) repository.restore(deleted)
                        }
                    },
                    onSaveNotes = { target, text -> scope.launch { repository.saveNotes(target, text) } },
                )
            }
        }
    }

    if (confirmReplace) {
        AlertDialog(
            onDismissRequest = { confirmReplace = false },
            title = { Text("Trocar de sessão?") },
            text = { Text("O cronômetro de “${session?.subject.orEmpty()}” já começou. Planejar uma nova sessão descarta esse cronômetro.") },
            confirmButton = { TextButton(onClick = { confirmReplace = false; planSession() }) { Text("Trocar") } },
            dismissButton = { TextButton(onClick = { confirmReplace = false; openTab(TAB_TIMER) }) { Text("Ver cronômetro") } },
        )
    }
}
