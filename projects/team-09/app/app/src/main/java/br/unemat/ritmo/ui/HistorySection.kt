package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import br.unemat.ritmo.data.HistorySummary
import br.unemat.ritmo.data.SessionRepository
import br.unemat.ritmo.data.StudySession
import br.unemat.ritmo.model.WeeklyGoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Mostra os dados lidos do banco: meta da semana, totais e as sessões. */
@Composable
fun HistorySection(
    summary: HistorySummary,
    sessions: List<StudySession>,
    weekMinutes: Int,
    weeklyGoal: Int,
    onGoalChange: (Int) -> Unit,
    onDelete: (StudySession) -> Unit,
    onSaveNotes: (StudySession, String) -> Unit,
) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    // Sprint 07: tocar em uma sessão abre os detalhes; Anotar abre a edição. Guardados pelo id (sobrevivem a girar a tela).
    var detailsId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editing = sessions.firstOrNull { it.id == editingId }
    val details = sessions.firstOrNull { it.id == detailsId }
    if (editing != null) {
        NotesDialog(
            session = editing,
            onDismiss = { editingId = null },
            onSave = { text -> onSaveNotes(editing, text); editingId = null }
        )
    } else if (details != null) {
        SessionDetailsDialog(
            session = details,
            onClose = { detailsId = null },
            onNotes = { editingId = details.id }
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WeeklyGoalCard(weekMinutes, weeklyGoal, onGoalChange)
        Text("Seu histórico", style = MaterialTheme.typography.titleLarge)
        if (summary.count == 0) {
            Text(
                "Nenhuma sessão salva ainda. Abra um plano e toque em Salvar no histórico.",
                color = Muted, style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(summaryText(summary), color = Muted, style = MaterialTheme.typography.bodyMedium)
            val visible = if (showAll) sessions else sessions.take(SessionRepository.RECENT_LIMIT)
            visible.forEach { session -> key(session.id) { SessionRow(session, onDelete, onOpen = { detailsId = session.id }) } }
            if (sessions.size > SessionRepository.RECENT_LIMIT) {
                TextButton(onClick = { showAll = !showAll }) {
                    Text(if (showAll) "Mostrar menos" else "Ver todas as ${sessions.size} sessões")
                }
            }
        }
    }
}

@Composable
private fun WeeklyGoalCard(weekMinutes: Int, goal: Int, onGoalChange: (Int) -> Unit) {
    Surface(color = Lime, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("META DA SEMANA", style = MaterialTheme.typography.labelMedium, color = Forest)
            Text(
                "${WeeklyGoal.format(weekMinutes)} de ${WeeklyGoal.format(goal)}",
                style = MaterialTheme.typography.titleLarge, color = Forest
            )
            LinearProgressIndicator(
                progress = { WeeklyGoal.progress(weekMinutes, goal) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Forest, trackColor = Color.White,
            )
            Text(
                if (weekMinutes >= goal) "Meta da semana cumprida."
                else "Faltam ${WeeklyGoal.format(goal - weekMinutes)} nesta semana.",
                style = MaterialTheme.typography.bodyMedium, color = Forest
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mudar a meta:", style = MaterialTheme.typography.bodyMedium, color = Forest, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { onGoalChange(WeeklyGoal.decrease(goal)) }, enabled = goal > WeeklyGoal.MIN) {
                    Text("− 30 min")
                }
                OutlinedButton(onClick = { onGoalChange(WeeklyGoal.increase(goal)) }, enabled = goal < WeeklyGoal.MAX) {
                    Text("+ 30 min")
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: StudySession, onDelete: (StudySession) -> Unit, onOpen: () -> Unit) {
    // Linha compacta: a anotação só aparece nos detalhes, para a lista não crescer.
    Surface(onClick = onOpen, color = Color(0xFFE8ECE2), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(session.subject, style = MaterialTheme.typography.titleMedium)
                Text("${session.minutes} min · ${formatDate(session.createdAt)}", color = Muted, style = MaterialTheme.typography.bodySmall)
                session.goal?.let { Text("Objetivo: $it", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                if (session.notes != null) Text("Com anotação", style = MaterialTheme.typography.labelMedium, color = Forest)
            }
            TextButton(
                onClick = { onDelete(session) },
                modifier = Modifier.semantics { contentDescription = "Excluir ${session.subject}" }
            ) { Text("Excluir") }
        }
    }
}

/** Sprint 07: detalhes da sessão, com o que foi estudado e o botão para anotar. */
@Composable
private fun SessionDetailsDialog(session: StudySession, onClose: () -> Unit, onNotes: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(session.subject) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${session.minutes} min · ${formatDate(session.createdAt)}", color = Muted, style = MaterialTheme.typography.bodyMedium)
                session.goal?.let { Text("Objetivo: $it", style = MaterialTheme.typography.bodyMedium) }
                Text("O QUE VOCÊ FEZ", style = MaterialTheme.typography.labelMedium, color = Muted)
                Text(
                    session.notes ?: "Nada anotado ainda.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (session.notes == null) Muted else Forest
                )
            }
        },
        confirmButton = { TextButton(onClick = onNotes) { Text(if (session.notes == null) "Anotar" else "Editar anotação") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Fechar") } },
    )
}

/** Sprint 07: caixa para escrever o que foi estudado ou a atividade feita na sessão. */
@Composable
private fun NotesDialog(session: StudySession, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by rememberSaveable(session.id) { mutableStateOf(session.notes.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("O que você estudou?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(session.subject, color = Muted, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= SessionRepository.NOTES_MAX) text = it },
                    label = { Text("Anotação") },
                    placeholder = { Text("Ex.: fiz 5 exercícios de limites e revisei a teoria") },
                    supportingText = { Text("${text.length}/${SessionRepository.NOTES_MAX}") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(text) }) { Text("Salvar anotação") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

fun summaryText(summary: HistorySummary): String {
    val sessions = if (summary.count == 1) "1 sessão salva" else "${summary.count} sessões salvas"
    return "$sessions · ${summary.totalMinutes} minutos no total"
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM 'às' HH:mm", Locale("pt", "BR")).format(Date(millis))
