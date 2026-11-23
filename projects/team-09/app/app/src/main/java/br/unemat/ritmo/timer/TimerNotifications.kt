package br.unemat.ritmo.timer

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.unemat.ritmo.MainActivity
import br.unemat.ritmo.R
import br.unemat.ritmo.model.StudyTimer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fronteira com as notificações do Android. Só este arquivo conversa com o NotificationManager.
 * - Contagem: notificação fixa enquanto há sessão começada, com o tempo que falta e o botão Pausar/Continuar.
 * - Alarme: no fim do tempo, toca o som de alarme em repetição por até 1 minuto ou até tocar em Parar.
 */
object TimerNotifications {
    const val CHANNEL_TIMER = "cronometro"
    const val CHANNEL_ALARM = "alarme_fim_da_sessao"
    const val ONGOING_ID = 7
    const val ALARM_ID = 8
    const val ALARM_DURATION_MS = 60_000L
    const val EXTRA_TAB = "aba"

    /** Android 13+ exige a permissão POST_NOTIFICATIONS; em versões anteriores ela é concedida na instalação. */
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Permissão concedida e notificações do app ligadas nas configurações. */
    fun canNotify(context: Context): Boolean =
        hasPermission(context) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun alarmTitle(subject: String) = "Sessão concluída: $subject"
    fun alarmText(minutes: Int) = "Seus $minutes minutos acabaram. Hora de fazer uma pausa."

    fun showRunning(context: Context, session: ActiveSession) {
        val endAt = session.timer.endAt ?: return
        val notification = base(context, CHANNEL_TIMER)
            .setContentTitle("Estudando: ${session.subject}")
            .setContentText("Alarme às ${formatTime(endAt)}")
            .setWhen(endAt)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .addAction(0, "Pausar", action(context, TimerReceiver.ACTION_PAUSE))
            .build()
        post(context, ONGOING_ID, notification)
    }

    fun showPaused(context: Context, session: ActiveSession) {
        val notification = base(context, CHANNEL_TIMER)
            .setContentTitle("Pausado: ${session.subject}")
            .setContentText("Faltam ${StudyTimer.format(session.timer.leftMs)}. O alarme volta quando você continuar.")
            .setShowWhen(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .addAction(0, "Continuar", action(context, TimerReceiver.ACTION_RESUME))
            .build()
        post(context, ONGOING_ID, notification)
    }

    fun showAlarm(context: Context, session: ActiveSession) {
        cancelOngoing(context)
        val notification = base(context, CHANNEL_ALARM)
            .setContentTitle(alarmTitle(session.subject))
            .setContentText(alarmText(session.minutes))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound(), AudioManager.STREAM_ALARM) // Android 7 (no 8+ o som vem do canal)
            .setVibrate(VIBRATION)
            .setTimeoutAfter(ALARM_DURATION_MS) // some (e o som para) depois de 1 minuto
            .setAutoCancel(true)
            .addAction(0, "Parar", action(context, TimerReceiver.ACTION_STOP))
            .build()
        // INSISTENT: o som se repete até a notificação sumir (Parar, tocar nela ou fim do minuto).
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        post(context, ALARM_ID, notification)
    }

    fun cancelOngoing(context: Context) = NotificationManagerCompat.from(context).cancel(ONGOING_ID)

    fun cancelAlarm(context: Context) = NotificationManagerCompat.from(context).cancel(ALARM_ID)

    fun isAlarmShowing(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java).activeNotifications.any { it.id == ALARM_ID }

    private fun base(context: Context, channel: String): NotificationCompat.Builder {
        createChannels(context)
        val openTimer = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_TAB, "cronometro")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openTimer)
    }

    private fun action(context: Context, action: String): PendingIntent = PendingIntent.getBroadcast(
        context, action.hashCode(),
        Intent(context, TimerReceiver::class.java).setAction(action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun post(context: Context, id: Int, notification: Notification) {
        if (!canNotify(context)) return // sem permissão: o cronômetro na tela continua funcionando
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // permissão retirada entre a verificação e o envio
        }
    }

    /** Canais exigidos a partir do Android 8; criar de novo não tem efeito. */
    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_TIMER, "Cronômetro", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Mostra o tempo que falta enquanto a sessão está rodando."; setShowBadge(false) }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ALARM, "Alarme do fim da sessão", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Toca quando o tempo da sessão de estudo acaba."
                setSound(
                    alarmSound(),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
                vibrationPattern = VIBRATION
            }
        )
    }

    private fun alarmSound() =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    private val VIBRATION = longArrayOf(0, 600, 400, 600)

    fun formatTime(millis: Long): String = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date(millis))
}
