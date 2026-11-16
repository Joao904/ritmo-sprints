package br.unemat.ritmo.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.unemat.ritmo.MainActivity
import br.unemat.ritmo.R

/**
 * Fronteira com o sistema de notificações do Android (SPEC-006).
 * Só este arquivo conversa com o NotificationManager.
 */
object SessionNotifier {
    const val CHANNEL_ID = "fim_da_sessao"
    const val NOTIFICATION_ID = 6

    /** Android 13+ exige a permissão POST_NOTIFICATIONS; em versões anteriores ela é concedida na instalação. */
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Permissão concedida e notificações do app ligadas nas configurações. */
    fun canNotify(context: Context): Boolean =
        hasPermission(context) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** Canal exigido a partir do Android 8; criar de novo não tem efeito. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Fim da sessão", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Avisa quando o tempo da sessão de estudo acaba." }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun title(subject: String) = "Sessão concluída: $subject"
    fun text(minutes: Int) = "Seus $minutes minutos acabaram. Hora de fazer uma pausa."

    /**
     * Mostra a notificação de fim da sessão. Devolve false, sem erro, quando o aviso
     * não pode ser exibido (permissão retirada ou notificações desligadas depois do agendamento).
     */
    fun show(context: Context, subject: String, minutes: Int): Boolean {
        if (!canNotify(context)) return false
        createChannel(context)
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title(subject))
            .setContentText(text(minutes))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        } catch (e: SecurityException) {
            false // permissão retirada entre a verificação e o envio
        }
    }
}
