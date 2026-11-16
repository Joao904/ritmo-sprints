package br.unemat.ritmo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.unemat.ritmo.reminder.SessionNotifier
import br.unemat.ritmo.reminder.SessionReminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Aviso de fim da sessão (Sprint 06). Estados: sem aviso, aviso marcado e sem permissão.
 * O pedido de permissão é assíncrono: o resultado chega no callback do launcher.
 */
@Composable
fun ReminderCard(subject: String, minutes: Int) {
    val context = LocalContext.current
    // Horário previsto do aviso; null = nenhum aviso marcado nesta tela.
    var endAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var denied by rememberSaveable { mutableStateOf(false) }

    fun schedule() {
        endAt = SessionReminder.schedule(context, subject, minutes)
        denied = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && SessionNotifier.canNotify(context)) schedule() else denied = true
    }

    fun requestReminder() {
        when {
            SessionNotifier.canNotify(context) -> schedule()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !SessionNotifier.hasPermission(context) ->
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            else -> denied = true // notificações desligadas nas configurações do aparelho
        }
    }

    Surface(color = Color(0xFFE8ECE2), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("AVISO DE FIM DA SESSÃO", style = MaterialTheme.typography.labelMedium, color = Muted)
            val scheduledAt = endAt
            when {
                scheduledAt != null -> {
                    Text("Aviso marcado para ${formatTime(scheduledAt)}.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Pode fechar o app: você recebe uma notificação quando os $minutes minutos acabarem.",
                        color = Muted, style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = { SessionReminder.cancel(context); endAt = null }, contentPadding = PaddingValues(0.dp)) {
                        Text("Cancelar aviso")
                    }
                }
                denied -> {
                    Text("Sem permissão de notificação.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Text(
                        "O Ritmo não consegue avisar quando o tempo acabar. O plano continua valendo: acompanhe pelo relógio ou ative as notificações nas configurações.",
                        color = Muted, style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { openNotificationSettings(context) }) { Text("Abrir configurações") }
                        TextButton(onClick = { requestReminder() }) { Text("Tentar de novo") }
                    }
                }
                else -> {
                    Text(
                        "Receba uma notificação quando os $minutes minutos acabarem, mesmo com o app fechado.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FilledTonalButton(
                        onClick = { requestReminder() },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Avisar quando acabar") }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date(millis))

/** Abre a tela de notificações do app (Android 8+) ou os detalhes do app (versões anteriores). */
private fun openNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
    }
    context.startActivity(intent)
}
