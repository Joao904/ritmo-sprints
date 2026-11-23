package br.unemat.ritmo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.unemat.ritmo.model.StudyTimer
import br.unemat.ritmo.timer.ActiveSession
import br.unemat.ritmo.timer.SessionClock
import br.unemat.ritmo.timer.TimerNotifications
import kotlinx.coroutines.delay

/**
 * Aba Cronômetro: o plano da sessão, o cronômetro e o alarme do fim.
 * Sprint 07: o cronômetro vem do SessionClock, então continua contando ao trocar de aba ou fechar o app,
 * e o alarme é marcado sozinho ao tocar em Começar.
 */
@Composable
fun PlanScreen(
    session: ActiveSession?,
    onSave: (ActiveSession) -> Unit,
    onAdjust: () -> Unit,
    modifier: Modifier = Modifier
) {
    val column = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    if (session == null) {
        Column(column, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Brand()
            Text("Nenhuma sessão planejada.", style = MaterialTheme.typography.headlineMedium)
            Text("Na aba Planejar, informe o tempo e o assunto e toque em Planejar meu estudo.", color = Muted)
            Button(onClick = onAdjust, shape = RoundedCornerShape(18.dp), modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Ir para Planejar")
            }
        }
        return
    }

    val context = LocalContext.current
    // Toques só valem com a tela já aberta: durante a animação de entrada (tela ainda não RESUMED),
    // o segundo toque de um toque duplo em Planejar meu estudo não aciona nada aqui.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val canTap = { lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }

    val timer = session.timer
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timer) {
        while (true) {
            now = System.currentTimeMillis()
            if (!timer.running || timer.finished(now)) break
            delay(250)
        }
    }

    // Permissão de notificação: conferida de novo sempre que a tela volta (o estudante pode mudar nas configurações).
    var canNotify by remember { mutableStateOf(TimerNotifications.canNotify(context)) }
    var asked by remember { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { canNotify = TimerNotifications.canNotify(context) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        canNotify = TimerNotifications.canNotify(context)
        SessionClock.refresh(context) // com a permissão, já mostra a contagem na notificação
    }
    fun askPermission() {
        asked = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !TimerNotifications.hasPermission(context)) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(column, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(Modifier.fillMaxWidth()) {
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
                Text("${session.minutes} minutos", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Text("Assunto: ${session.subject}", color = Color.White, style = MaterialTheme.typography.titleMedium)
                if (session.goal.isNotEmpty()) {
                    Text("Objetivo: ${session.goal}", color = Color(0xFFD6E3DD))
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                TimerPanel(
                    timer = timer,
                    now = now,
                    onStart = {
                        if (canTap()) {
                            SessionClock.start(context)
                            if (!canNotify && !asked) askPermission()
                        }
                    },
                    onPause = { SessionClock.pause(context) },
                    onReset = { SessionClock.reset(context) },
                )
            }
        }
        AlarmCard(
            timer = timer,
            now = now,
            canNotify = canNotify,
            canAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !TimerNotifications.hasPermission(context),
            onAsk = { askPermission() },
            onStop = { SessionClock.stopAlarm(context) },
        )
        Text("Um roteiro simples", style = MaterialTheme.typography.titleLarge)
        PlanStep("01", "Prepare seu espaço", "Separe o material de ${session.subject} e deixe o resto de lado.")
        PlanStep("02", "Concentre-se", "Dedique os ${session.minutes} minutos escolhidos ao assunto, respeitando seu ritmo.")
        PlanStep("03", "Faça uma pausa", "Ao terminar, levante-se, respire e reconheça o que aprendeu.")
        Button(
            onClick = { if (canTap()) onSave(session) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Salvar no histórico", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(onClick = onAdjust, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) {
            Text("Ajustar duração", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            "O cronômetro continua contando se você trocar de aba ou fechar o app. Ele só para quando você toca em Pausar.",
            color = Muted, style = MaterialTheme.typography.bodySmall
        )
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

/** Estado do alarme do fim: marcado sozinho com o cronômetro, pausado junto com ele, tocando ou sem permissão. */
@Composable
private fun AlarmCard(timer: StudyTimer, now: Long, canNotify: Boolean, canAsk: Boolean, onAsk: () -> Unit, onStop: () -> Unit) {
    Surface(color = Color(0xFFE8ECE2), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("ALARME DO FIM DA SESSÃO", style = MaterialTheme.typography.labelMedium, color = Muted)
            when {
                !canNotify -> {
                    Text("Sem permissão de notificação.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Text(
                        "O cronômetro continua contando, mas o alarme não vai tocar quando o tempo acabar. Para ouvir o alarme, ative as notificações do Ritmo.",
                        color = Muted, style = MaterialTheme.typography.bodyMedium
                    )
                    val context = LocalContext.current
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { openNotificationSettings(context) }) { Text("Abrir configurações") }
                        if (canAsk) TextButton(onClick = onAsk) { Text("Permitir") }
                    }
                }
                timer.finished(now) -> {
                    Text("O tempo acabou.", style = MaterialTheme.typography.titleMedium)
                    Text("O alarme toca por até 1 minuto.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    FilledTonalButton(onClick = onStop, modifier = Modifier.heightIn(min = 48.dp)) { Text("Parar alarme") }
                }
                timer.running -> {
                    Text("Alarme marcado para ${TimerNotifications.formatTime(timer.endAt!!)}.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Pode trocar de aba ou fechar o app: o alarme toca por até 1 minuto quando o tempo acabar.",
                        color = Muted, style = MaterialTheme.typography.bodyMedium
                    )
                }
                timer.started(now) -> {
                    Text("Alarme pausado junto com o cronômetro.", style = MaterialTheme.typography.titleMedium)
                    Text("Quando você continuar, o alarme é marcado de novo para o fim do tempo que falta.",
                        color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
                else -> Text(
                    "O alarme é marcado sozinho quando você tocar em Começar.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PlanStep(number: String, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = Lime, shape = RoundedCornerShape(12.dp)) {
            Box(Modifier.size(40.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(number, style = MaterialTheme.typography.labelLarge, color = Forest)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Abre a tela de notificações do app (Android 8+) ou os detalhes do app (versões anteriores). */
private fun openNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
    }
    context.startActivity(intent)
}
