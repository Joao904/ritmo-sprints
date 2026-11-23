package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.unemat.ritmo.data.HistorySummary
import br.unemat.ritmo.data.StudySession

/** Sprint 07: aba Histórico, com a meta da semana e as sessões salvas (antes ficavam no fim da tela inicial). */
@Composable
fun HistoryScreen(
    summary: HistorySummary,
    sessions: List<StudySession>,
    weekMinutes: Int,
    weeklyGoal: Int,
    onWeeklyGoalChange: (Int) -> Unit,
    onDelete: (StudySession) -> Unit,
    onSaveNotes: (StudySession, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Brand()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Seu progresso", style = MaterialTheme.typography.headlineLarge)
            Text("O que você já estudou e quanto falta para a meta.", color = Muted, style = MaterialTheme.typography.bodyLarge)
        }
        HistorySection(
            summary = summary,
            sessions = sessions,
            weekMinutes = weekMinutes,
            weeklyGoal = weeklyGoal,
            onGoalChange = onWeeklyGoalChange,
            onDelete = onDelete,
            onSaveNotes = onSaveNotes
        )
    }
}
