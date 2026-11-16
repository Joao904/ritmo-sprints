package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.unemat.ritmo.model.StudyTimer
import br.unemat.ritmo.reminder.SessionReminder
import kotlinx.coroutines.delay

private val TimerSaver = listSaver<StudyTimer, Long>(save = { it.toList() }, restore = { StudyTimer.fromList(it) })

@Composable
fun PlanScreen(
    selectedMinutes: Int,
    subject: String,
    goal: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Toques só valem com a tela já aberta: durante a animação de entrada (tela ainda não RESUMED),
    // o segundo toque de um toque duplo em Planejar meu estudo não aciona nada aqui.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val canTap = { lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }

    // Disables the button after the first tap, so a double tap cannot save twice.
    var saved by remember { mutableStateOf(false) }

    // Cronômetro: guarda o horário de término, então continua certo com o app em segundo plano.
    var timer by rememberSaveable(stateSaver = TimerSaver) { mutableStateOf(StudyTimer.ofMinutes(selectedMinutes)) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timer.endAt) {
        while (timer.running) {
            now = System.currentTimeMillis()
            if (timer.finished(now)) timer = timer.pause(now) else delay(250)
        }
        now = System.currentTimeMillis()
    }

    // Aviso por notificação: segue o cronômetro (pausa junto e é remarcado ao continuar).
    var reminderOn by rememberSaveable { mutableStateOf(false) }
    var reminderAt by rememberSaveable { mutableStateOf<Long?>(null) }
    fun syncReminder(t: StudyTimer) {
        val current = System.currentTimeMillis()
        val paused = t.started(current) && !t.running
        if (!reminderOn || paused) {
            SessionReminder.cancel(context)
            reminderAt = null
        } else {
            reminderAt = SessionReminder.schedule(context, subject, selectedMinutes, current, delayMs = t.remaining(current))
        }
    }
    fun updateTimer(t: StudyTimer) {
        timer = t
        now = System.currentTimeMillis()
        if (reminderOn) syncReminder(t)
    }

    Scaffold(containerColor = Paper) { padding ->
        Column(
            modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("Voltar") }
                Spacer(Modifier.weight(1f))
                Text("RITMO / SEU ESTUDO", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Seu plano", style = MaterialTheme.typography.headlineLarge)
                Text("Um tempo reservado para aprender.", color = Muted, style = MaterialTheme.typography.bodyLarge)
            }
            Surface(color = Forest, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TEMPO DE CONCENTRAÇÃO", color = Lime, style = MaterialTheme.typography.labelMedium)
                    Text("$selectedMinutes minutos", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Text("Assunto: $subject", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    if (goal.isNotEmpty()) {
                        Text("Objetivo: $goal", color = Color(0xFFD6E3DD))
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    TimerPanel(
                        timer = timer,
                        now = now,
                        onStart = { if (canTap()) updateTimer(timer.start(System.currentTimeMillis())) },
                        onPause = { updateTimer(timer.pause(System.currentTimeMillis())) },
                        onReset = { updateTimer(timer.reset()) },
                    )
                }
            }
            // Sprint 06: integração com as notificações do aparelho, logo abaixo do tempo da sessão.
            ReminderCard(
                minutes = selectedMinutes,
                active = reminderOn,
                scheduledAt = reminderAt,
                onEnable = { reminderOn = true; syncReminder(timer) },
                onCancel = { reminderOn = false; syncReminder(timer) },
                canTap = canTap,
            )
            Text("Um roteiro simples", style = MaterialTheme.typography.titleLarge)
            PlanStep("01", "Prepare seu espaço", "Separe o material de $subject e deixe o resto de lado.")
            PlanStep("02", "Concentre-se", "Dedique os $selectedMinutes minutos escolhidos ao assunto, respeitando seu ritmo.")
            PlanStep("03", "Faça uma pausa", "Ao terminar, levante-se, respire e reconheça o que aprendeu.")
            Button(
                onClick = { if (canTap()) { saved = true; onSave() } },
                enabled = !saved,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Salvar no histórico", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Ajustar duração", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "O cronômetro conta aqui na tela; o aviso de fim chega por notificação mesmo com o app fechado.",
                color = Muted, style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** Cronômetro em regressiva com Começar, Pausar, Continuar e Recomeçar. */
@Composable
private fun TimerPanel(timer: StudyTimer, now: Long, onStart: () -> Unit, onPause: () -> Unit, onReset: () -> Unit) {
    val left = timer.remaining(now)
    val finished = timer.finished(now)
    val started = timer.started(now)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            StudyTimer.format(left),
            color = Lime,
            style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Monospace, fontSize = 56.sp, lineHeight = 60.sp)
        )
        LinearProgressIndicator(
            progress = { if (timer.totalMs == 0L) 0f else 1f - left.toFloat() / timer.totalMs },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = Lime, trackColor = Color.White.copy(alpha = 0.2f),
        )
        Text(
            when {
                finished -> "Tempo esgotado. Hora de fazer uma pausa."
                timer.running -> "Cronômetro rodando."
                started -> "Cronômetro pausado."
                else -> "Toque em Começar quando estiver pronto."
            },
            color = Color(0xFFD6E3DD), style = MaterialTheme.typography.bodyMedium
        )
        val limeButton = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Forest)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                finished -> Button(onClick = onReset, colors = limeButton) { Text("Recomeçar") }
                timer.running -> Button(onClick = onPause, colors = limeButton) { Text("Pausar") }
                else -> Button(onClick = onStart, colors = limeButton) { Text(if (started) "Continuar" else "Começar") }
            }
            if (started && !finished) {
                OutlinedButton(onClick = onReset, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                    Text("Recomeçar")
                }
            }
        }
    }
}

@Composable
private fun PlanStep(number: String, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = Lime, shape = RoundedCornerShape(12.dp)) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Text(number, style = MaterialTheme.typography.labelLarge, color = Forest)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanPreview() { RitmoTheme { PlanScreen(25, "Cálculo I", "resolver 5 exercícios", {}, {}) } }
