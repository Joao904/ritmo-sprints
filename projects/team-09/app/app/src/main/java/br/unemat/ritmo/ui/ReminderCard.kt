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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Aviso de fim da sessão (Sprint 06). Estados: sem aviso, aviso marcado, aviso pausado
 * (junto com o cronômetro) e sem permissão. O horário do aviso é controlado pela PlanScreen,
 * que o mantém igual ao fim do cronômetro.
 * O pedido de permissão é assíncrono: o resultado chega no callback do launcher.
 */
@Composable
fun ReminderCard(
    minutes: Int,
    active: Boolean,
    scheduledAt: Long?,
    onEnable: () -> Unit,
    onCancel: () -> Unit,
    canTap: () -> Boolean = { true },
) {
    val context = LocalContext.current
    var denied by rememberSaveable { mutableStateOf(false) }

    fun enable() {
        denied = false
        onEnable()
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && SessionNotifier.canNotify(context)) enable() else denied = true
    }

    fun requestReminder() {
        if (!canTap()) return
        when {
            SessionNotifier.canNotify(context) -> enable()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !SessionNotifier.hasPermission(context) ->
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            else -> denied = true // notificações desligadas nas configurações do aparelho
        }
    }

    Surface(color = Color(0xFFE8ECE2), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("AVISO DE FIM DA SESSÃO", style = MaterialTheme.typography.labelMedium, color = Muted)
            when {
                active && scheduledAt != null -> {
                    Text("Aviso marcado para ${formatTime(scheduledAt)}.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Pode fechar o app: você recebe uma notificação quando os $minutes minutos acabarem.",
                        color = Muted, style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onCancel, contentPadding = PaddingValues(0.dp)) { Text("Cancelar aviso") }
                }
                active -> {
                    Text("Aviso pausado junto com o cronômetro.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Quando você continuar, o aviso é marcado de novo para o fim do tempo que falta.",
                        color = Muted, style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onCancel, contentPadding = PaddingValues(0.dp)) { Text("Cancelar aviso") }
                }
                denied -> {
                    Text("Sem permissão de notificação.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Text(
                        "O Ritmo não consegue avisar com o app fechado. O cronômetro na tela continua funcionando; para receber o aviso, ative as notificações nas configurações.",
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
