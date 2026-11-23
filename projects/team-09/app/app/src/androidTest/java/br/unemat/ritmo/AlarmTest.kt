package br.unemat.ritmo

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import br.unemat.ritmo.timer.AlarmScheduler
import br.unemat.ritmo.timer.TimerNotifications
import br.unemat.ritmo.timer.TimerReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Sprint 07: alarme do fim marcado sozinho ao começar o cronômetro, notificação com a contagem
 * e alarme que toca por até 1 minuto ou até tocar em Parar.
 */
class AlarmTest {
    @get:Rule(order = 0) val clear = ClearDatabaseRule()
    @get:Rule(order = 1) val permission = notificationPermission()
    @get:Rule(order = 2) val compose = createAndroidComposeRule<MainActivity>()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val manager get() = context.getSystemService(NotificationManager::class.java)

    /** O sistema publica e remove notificações de forma assíncrona: espera até 5 s. */
    private fun waitForNotification(id: Int, present: Boolean = true): StatusBarNotification? {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            val found = manager.activeNotifications.firstOrNull { it.id == id }
            if ((found != null) == present) return found
            Thread.sleep(200)
        }
        return manager.activeNotifications.firstOrNull { it.id == id }
    }

    private fun send(action: String) = instrumentation.runOnMainSync {
        TimerReceiver().onReceive(context, Intent(context, TimerReceiver::class.java).setAction(action))
    }

    @Test fun startSchedulesTheAlarmAndPauseCancelsIt() {
        compose.planSession("30", "Cálculo I")
        compose.onNodeWithText("O alarme é marcado sozinho quando você tocar em Começar.").performScrollTo().assertIsDisplayed()
        assertFalse(AlarmScheduler.isScheduled(context))

        compose.tap("Começar")
        compose.waitForText("Alarme marcado para", substring = true)
        assertTrue(AlarmScheduler.isScheduled(context))
        val ongoing = waitForNotification(TimerNotifications.ONGOING_ID)
        assertNotNull("sem a notificação com a contagem", ongoing)
        assertEquals("Estudando: Cálculo I", ongoing!!.notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString())

        compose.tap("Pausar")
        compose.waitForText("Alarme pausado junto com o cronômetro.")
        assertFalse(AlarmScheduler.isScheduled(context))

        compose.tap("Continuar")
        compose.waitForText("Alarme marcado para", substring = true)
        assertTrue(AlarmScheduler.isScheduled(context))

        compose.onAllNodesWithText("Recomeçar")[0].performScrollTo().performClick()
        compose.waitForText("O alarme é marcado sozinho quando você tocar em Começar.")
        assertFalse(AlarmScheduler.isScheduled(context))
        assertNull(waitForNotification(TimerNotifications.ONGOING_ID, present = false))
    }

    @Test fun notificationButtonsPauseAndContinueTheTimer() {
        compose.planSession("30", "Cálculo I")
        compose.tap("Começar")
        compose.waitForText("Cronômetro rodando.")
        send(TimerReceiver.ACTION_PAUSE)
        compose.waitForText("Cronômetro pausado.")
        assertFalse(AlarmScheduler.isScheduled(context))
        send(TimerReceiver.ACTION_RESUME)
        compose.waitForText("Cronômetro rodando.")
        assertTrue(AlarmScheduler.isScheduled(context))
    }

    @Test fun alarmRingsAtTheEndAndStops() {
        compose.planSession("30", "Cálculo I")
        compose.tap("Começar")
        compose.waitForText("Cronômetro rodando.")
        // O mesmo aviso que o AlarmManager entrega no fim do tempo, sem esperar os 30 minutos.
        send(TimerReceiver.ACTION_ALARM)

        compose.waitForText("Tempo esgotado. Hora de fazer uma pausa.")
        compose.onNodeWithText("00:00").assertExists()
        val alarm = waitForNotification(TimerNotifications.ALARM_ID)
        assertNotNull("o alarme não apareceu", alarm)
        val notification = alarm!!.notification
        assertEquals("Sessão concluída: Cálculo I", notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
        assertEquals("Seus 30 minutos acabaram. Hora de fazer uma pausa.", notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
        assertTrue("o som do alarme precisa repetir", notification.flags and Notification.FLAG_INSISTENT != 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertEquals(TimerNotifications.ALARM_DURATION_MS, notification.timeoutAfter)
            val channel = manager.getNotificationChannel(TimerNotifications.CHANNEL_ALARM)
            assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
            assertNotNull("o canal do alarme precisa ter som", channel.sound)
        }
        assertEquals("Parar", notification.actions.single().title.toString())
        assertNull("a contagem some quando o alarme toca", waitForNotification(TimerNotifications.ONGOING_ID, present = false))

        compose.tap("Parar alarme")
        assertNull("o alarme não parou", waitForNotification(TimerNotifications.ALARM_ID, present = false))
    }

    @Test fun stopButtonOnTheNotificationSilencesTheAlarm() {
        compose.planSession("30", "Cálculo I")
        compose.tap("Começar")
        compose.waitForText("Cronômetro rodando.")
        send(TimerReceiver.ACTION_ALARM)
        assertNotNull(waitForNotification(TimerNotifications.ALARM_ID))
        send(TimerReceiver.ACTION_STOP)
        assertNull(waitForNotification(TimerNotifications.ALARM_ID, present = false))
    }

    @Test fun savingClearsTheTimerAndTheAlarm() {
        compose.planSession("30", "Cálculo I")
        compose.tap("Começar")
        compose.waitForText("Alarme marcado para", substring = true)
        compose.tap("Salvar no histórico")
        compose.waitForText("1 sessão salva · 30 minutos no total")
        assertFalse(AlarmScheduler.isScheduled(context))
        assertNull(waitForNotification(TimerNotifications.ONGOING_ID, present = false))
        compose.openTab("Cronômetro")
        compose.onNodeWithText("Nenhuma sessão planejada.").assertIsDisplayed()
    }
}
