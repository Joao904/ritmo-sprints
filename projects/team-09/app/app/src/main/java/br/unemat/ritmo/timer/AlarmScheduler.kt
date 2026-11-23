package br.unemat.ritmo.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Sprint 07: marca o alarme do fim da sessão no AlarmManager do Android.
 * O sistema acorda o app no horário exato, mesmo com o app fechado ou a tela desligada.
 * Só existe um alarme por vez: marcar de novo substitui o anterior.
 */
object AlarmScheduler {
    private const val REQUEST_ALARM = 70

    fun schedule(context: Context, at: Long) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val intent = alarmIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)!!
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
        try {
            if (exactAllowed) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
            else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        } catch (e: SecurityException) {
            // Permissão de alarme exato retirada: o alarme ainda toca, com alguns minutos de folga.
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
    }

    fun cancel(context: Context) {
        val intent = alarmIntent(context, PendingIntent.FLAG_NO_CREATE) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(intent)
        intent.cancel()
    }

    /** Há um alarme marcado? (usado pelos testes) */
    fun isScheduled(context: Context): Boolean = alarmIntent(context, PendingIntent.FLAG_NO_CREATE) != null

    private fun alarmIntent(context: Context, flag: Int): PendingIntent? = PendingIntent.getBroadcast(
        context, REQUEST_ALARM,
        Intent(context, TimerReceiver::class.java).setAction(TimerReceiver.ACTION_ALARM),
        PendingIntent.FLAG_IMMUTABLE or flag
    )
}

/** Recebe o alarme do fim e os botões das notificações (Pausar, Continuar e Parar). */
class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ALARM -> SessionClock.onAlarm(context)
            ACTION_PAUSE -> SessionClock.pause(context)
            ACTION_RESUME -> SessionClock.start(context)
            ACTION_STOP -> SessionClock.stopAlarm(context)
        }
    }

    companion object {
        const val ACTION_ALARM = "br.unemat.ritmo.FIM_DA_SESSAO"
        const val ACTION_PAUSE = "br.unemat.ritmo.PAUSAR"
        const val ACTION_RESUME = "br.unemat.ritmo.CONTINUAR"
        const val ACTION_STOP = "br.unemat.ritmo.PARAR_ALARME"
    }
}
