package br.unemat.ritmo

import android.app.NotificationManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import br.unemat.ritmo.reminder.SessionEndWorker
import br.unemat.ritmo.reminder.SessionReminder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.regex.Pattern

/**
 * Gera integration-alternative-state.png, integration-success.png e integration-notification.png.
 * Antes de cada passo, o script de validação retira a permissão de notificação do app,
 * então a janela de permissão do sistema aparece de verdade e é respondida pelo UiAutomator.
 */
class Sprint06Evidence {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun onlyWhenRequested() = Evidence.requireEvidenceRun()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val device get() = UiDevice.getInstance(instrumentation)

    private fun openPlanAndAskForReminder() {
        compose.onNodeWithText("Minutos").performScrollTo().performTextReplacement("30")
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Cálculo I — limites")
        compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextReplacement("resolver 5 exercícios")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Avisar quando acabar").performScrollTo().performClick()
    }

    /** Toca em um botão da janela de permissão do sistema (pelo id ou, se mudar, pelo texto). */
    private fun answerPermissionDialog(resId: String, text: Pattern) {
        val button = device.wait(Until.findObject(By.res("com.android.permissioncontroller", resId)), 10_000)
            ?: device.wait(Until.findObject(By.text(text)), 5_000)
        assertNotNull("janela de permissão não apareceu", button)
        button.click()
    }

    @Test fun notificationDenied() {
        openPlanAndAskForReminder()
        answerPermissionDialog("permission_deny_button", Pattern.compile("(?i)don.t allow|não permitir"))
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Sem permissão de notificação.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Abrir configurações").performScrollTo()
        Evidence.screenshot("integration-alternative-state")
    }

    @Test fun notificationAllowed() {
        openPlanAndAskForReminder()
        answerPermissionDialog("permission_allow_button", Pattern.compile("(?i)^allow$|^permitir$"))
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Aviso marcado para", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Cancelar aviso").performScrollTo()
        Evidence.screenshot("integration-success")

        // Fim da sessão: executa o Worker agendado sem esperar os 30 minutos e abre a gaveta de notificações.
        val worker = TestListenableWorkerBuilder<SessionEndWorker>(instrumentation.targetContext)
            .setInputData(workDataOf(SessionReminder.KEY_SUBJECT to "Cálculo I — limites", SessionReminder.KEY_MINUTES to 30))
            .build()
        runBlocking { worker.doWork() }
        device.openNotification()
        assertNotNull(device.wait(Until.findObject(By.textContains("Sessão concluída")), 10_000))
        Evidence.screenshot("integration-notification")
        device.pressBack()
        SessionReminder.cancel(instrumentation.targetContext)
        instrumentation.targetContext.getSystemService(NotificationManager::class.java).cancelAll()
    }
}
