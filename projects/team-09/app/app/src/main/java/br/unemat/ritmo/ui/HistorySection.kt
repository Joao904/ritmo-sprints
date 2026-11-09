package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.unemat.ritmo.data.HistorySummary
import br.unemat.ritmo.data.StudySession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Mostra os dados lidos do banco: totais e as sessões mais recentes. */
@Composable
fun HistorySection(summary: HistorySummary, sessions: List<StudySession>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Seu histórico", style = MaterialTheme.typography.titleLarge)
        if (summary.count == 0) {
            Text(
                "Nenhuma sessão salva ainda. Abra um plano e toque em Salvar no histórico.",
                color = Muted, style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(summaryText(summary), color = Muted, style = MaterialTheme.typography.bodyMedium)
            sessions.forEach { SessionRow(it) }
        }
    }
}

@Composable
private fun SessionRow(session: StudySession) {
    Surface(color = Color(0xFFE8ECE2), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(session.subject, style = MaterialTheme.typography.titleMedium)
            Text("${session.minutes} min · ${formatDate(session.createdAt)}", color = Muted, style = MaterialTheme.typography.bodySmall)
            session.goal?.let { Text("Objetivo: $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

fun summaryText(summary: HistorySummary): String {
    val sessions = if (summary.count == 1) "1 sessão salva" else "${summary.count} sessões salvas"
    return "$sessions · ${summary.totalMinutes} minutos no total"
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM 'às' HH:mm", Locale("pt", "BR")).format(Date(millis))
