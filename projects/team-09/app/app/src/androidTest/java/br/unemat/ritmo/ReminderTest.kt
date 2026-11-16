package br.unemat.ritmo

import android.Manifest
import android.app.NotificationManager
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import br.unemat.ritmo.reminder.SessionEndWorker
import br.unemat.ritmo.reminder.SessionNotifier
import br.unemat.ritmo.reminder.SessionReminder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

/**
 * Sprint 06 com a permissão concedida (o caso negado é validado por Sprint06Evidence,
 * que toca no botão "Não permitir" da janela do sistema).
 */
class ReminderTest {
    // Antes do Android 13 a permissão não existe em tempo de execução: nada a conceder.
    @get:Rule val permission: TestRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        else TestRule { base, _ -> base }
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @After fun cleanUp() {
        SessionReminder.cancel(context)
        context.getSystemService(NotificationManager::class.java).cancelAll()
    }

    private fun reminderState(): WorkInfo.State? =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(SessionReminder.WORK_NAME).get().lastOrNull()?.state

    private fun openPlan(minutes: String) {
        compose.onNodeWithText("Minutos").performScrollTo().performTextReplacement(minutes)
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Cálculo I")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
    }

    @Test fun reminderIsScheduledAndCanBeCancelled() {
        openPlan("30")
        compose.onNodeWithText("Avisar quando acabar").performScrollTo().performClick()
        compose.onNodeWithText("Aviso marcado para", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("quando os 30 minutos acabarem", substring = true).assertExists()
        assertEquals(WorkInfo.State.ENQUEUED, reminderState())

        compose.onNodeWithText("Cancelar aviso").performClick()
        compose.onNodeWithText("Avisar quando acabar").performScrollTo().assertIsDisplayed()
        assertEquals(WorkInfo.State.CANCELLED, reminderState())
    }

    @Test fun reminderFollowsTheTimer() {
        openPlan("30")
        compose.onNodeWithText("Avisar quando acabar").performScrollTo().performClick()
        compose.onNodeWithText("Aviso marcado para", substring = true).assertExists()
        compose.onNodeWithText("Começar").performScrollTo().performClick()
        assertEquals(WorkInfo.State.ENQUEUED, reminderState())

        // Pausar o cronômetro pausa o aviso; continuar marca de novo.
        compose.onNodeWithText("Pausar").performClick()
        compose.onNodeWithText("Aviso pausado junto com o cronômetro.").performScrollTo().assertIsDisplayed()
        assertEquals(WorkInfo.State.CANCELLED, reminderState())
        compose.onNodeWithText("Continuar").performScrollTo().performClick()
        compose.onNodeWithText("Aviso marcado para", substring = true).performScrollTo().assertIsDisplayed()
        assertEquals(WorkInfo.State.ENQUEUED, reminderState())
    }

    @Test fun schedulingAgainReplacesThePreviousReminder() {
        SessionReminder.schedule(context, "Física", 20)
        SessionReminder.schedule(context, "Química", 40)
        val works = WorkManager.getInstance(context).getWorkInfosForUniqueWork(SessionReminder.WORK_NAME).get()
        assertEquals(1, works.count { it.state == WorkInfo.State.ENQUEUED })
    }

    @Test fun workerPostsTheEndOfSessionNotification() {
        // Executa o mesmo Worker que o WorkManager executaria, sem esperar o atraso.
        val worker = TestListenableWorkerBuilder<SessionEndWorker>(context)
            .setInputData(workDataOf(SessionReminder.KEY_SUBJECT to "Cálculo I", SessionReminder.KEY_MINUTES to 30))
            .build()
        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue("sem permissão de notificação no teste", SessionNotifier.canNotify(context))

        // O sistema publica a notificação de forma assíncrona: espera até 5 s.
        val manager = context.getSystemService(NotificationManager::class.java)
        var posted = manager.activeNotifications.firstOrNull { it.id == SessionNotifier.NOTIFICATION_ID }
        val deadline = System.currentTimeMillis() + 5_000
        while (posted == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(200)
            posted = manager.activeNotifications.firstOrNull { it.id == SessionNotifier.NOTIFICATION_ID }
        }
        assertTrue("notificação não encontrada", posted != null)
        val extras = posted!!.notification.extras
        assertEquals("Sessão concluída: Cálculo I", extras.getCharSequence("android.title").toString())
        assertEquals("Seus 30 minutos acabaram. Hora de fazer uma pausa.", extras.getCharSequence("android.text").toString())
    }
}
