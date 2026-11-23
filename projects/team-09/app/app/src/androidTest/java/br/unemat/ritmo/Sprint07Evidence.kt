package br.unemat.ritmo

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import br.unemat.ritmo.data.RitmoDatabase
import br.unemat.ritmo.data.StudySession
import br.unemat.ritmo.timer.SessionClock
import br.unemat.ritmo.timer.TimerReceiver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.regex.Pattern

/**
 * Gera as capturas da Sprint 07. Cada passo roda em um processo novo do app (o script fecha o app entre
 * um passo e outro) e começa sem a permissão de notificação, então a janela de permissão do sistema aparece de verdade.
 * Ordem no script: finalHome, timerStart, timerAfterRestart, finalAlarm, finalHistory, finalNotes.
 */
class Sprint07Evidence {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun onlyWhenRequested() = Evidence.requireEvidenceRun()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val device get() = UiDevice.getInstance(instrumentation)

    private fun allowInSystemDialog() {
        val allow = device.wait(Until.findObject(By.res("com.android.permissioncontroller", "permission_allow_button")), 10_000)
            ?: device.wait(Until.findObject(By.text(Pattern.compile("(?i)^allow$|^permitir$"))), 5_000)
        assertNotNull("janela de permissão não apareceu", allow)
        allow.click()
    }

    /** O script retira a permissão antes de cada passo; aqui ela é concedida de novo pelo cartão do alarme. */
    private fun allowFromAlarmCard() {
        compose.waitForIdle()
        if (compose.onAllNodesWithText("Permitir").fetchSemanticsNodes().isNotEmpty()) {
            compose.tap("Permitir")
            allowInSystemDialog()
            compose.waitForText("Alarme marcado para", substring = true)
        }
    }

    /** Aba Planejar logo depois de abrir o app, sem nenhum dado. */
    @Test fun finalHome() {
        compose.onNodeWithText("Planejar meu estudo").assertExists()
        Evidence.screenshot("final-home")
    }

    /** Planeja 50 min e começa o cronômetro; a permissão é concedida na janela do sistema. */
    @Test fun timerStart() {
        compose.planSession("50", "Cálculo I — limites", "resolver 5 exercícios")
        compose.tap("Começar")
        allowInSystemDialog()
        compose.waitForText("Alarme marcado para", substring = true)
        Thread.sleep(5_000) // deixa o cronômetro andar alguns segundos
        compose.onNodeWithText("Alarme marcado para", substring = true).performScrollTo()
        Evidence.screenshot("final-feature")
        // A contagem também aparece na notificação fixa, com o botão Pausar.
        device.openNotification()
        assertNotNull(device.wait(Until.findObject(By.textContains("Estudando: Cálculo I")), 10_000))
        Thread.sleep(1_000)
        Evidence.screenshot("final-notification-countdown")
        device.pressBack()
    }

    /** Processo novo: o app foi fechado no passo anterior e o cronômetro continuou rodando. */
    @Test fun timerAfterRestart() {
        compose.openTab("Cronômetro")
        compose.waitForText("Cronômetro rodando.")
        compose.onNodeWithText("Pausar").performScrollTo()
        Evidence.screenshot("final-timer-after-restart")
    }

    /** Fim do tempo: o mesmo aviso que o AlarmManager entrega, sem esperar os 50 minutos. */
    @Test fun finalAlarm() {
        compose.openTab("Cronômetro")
        compose.waitForText("Cronômetro rodando.")
        allowFromAlarmCard()
        instrumentation.runOnMainSync {
            TimerReceiver().onReceive(context, Intent(context, TimerReceiver::class.java).setAction(TimerReceiver.ACTION_ALARM))
        }
        compose.waitForText("Tempo esgotado. Hora de fazer uma pausa.")
        device.openNotification()
        assertNotNull(device.wait(Until.findObject(By.textContains("Sessão concluída")), 10_000))
        Thread.sleep(1_000)
        Evidence.screenshot("final-alarm")
        device.pressBack()
        SessionClock.stopAlarm(context)
    }

    /** Histórico e meta da semana: 4 sessões anteriores gravadas no banco e 1 salva pela interface. */
    @Test fun finalHistory() {
        ClearDatabaseRule.clear()
        val dao = RitmoDatabase.get(context).sessionDao()
        val now = System.currentTimeMillis()
        val minute = 60_000L
        runBlocking {
            listOf(
                Triple("Inglês — phrasal verbs", null, 20),
                Triple("Física II — leis de Newton", "fazer a lista 3", 45),
                Triple("Estatística — média e moda", null, 30),
                Triple("Anatomia — ossos do crânio", "revisar para a prova", 25),
            ).forEachIndexed { index, (subject, goal, minutes) ->
                dao.insert(StudySession(subject = subject, goal = goal, minutes = minutes, createdAt = now - (5 - index) * 15 * minute))
            }
        }
        compose.planSession("50", "Cálculo I — limites", "resolver 5 exercícios")
        compose.tap("Salvar no histórico")
        compose.waitForText("5 sessões salvas", substring = true)
        // Espera a mensagem "Sessão salva no histórico." sumir para ela não cobrir a lista.
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Sessão salva no histórico.").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Inglês — phrasal verbs").performScrollTo()
        Evidence.screenshot("final-history")
    }

    /** Anotação: toca na sessão salva no passo anterior, anota o que foi feito e captura a janela de detalhes. */
    @Test fun finalNotes() {
        compose.openTab("Histórico")
        compose.waitForText("5 sessões salvas", substring = true)
        compose.onNodeWithText("Cálculo I — limites").performScrollTo().performClick()
        compose.onNodeWithText("Anotar").performClick()
        compose.onNodeWithText("Anotação").performTextInput("Fiz os exercícios 1 a 5 da lista de limites e revisei a definição pelo livro.")
        compose.onNodeWithText("Salvar anotação").performClick()
        compose.waitForText("Fiz os exercícios 1 a 5", substring = true)
        Evidence.screenshot("final-notes")
    }
}
