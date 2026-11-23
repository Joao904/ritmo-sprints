package br.unemat.ritmo

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import br.unemat.ritmo.timer.SessionClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Cronômetro da aba Cronômetro. Sprint 07: continua contando fora da tela e só para no Pausar. */
class TimerTest {
    @get:Rule(order = 0) val clear = ClearDatabaseRule()
    @get:Rule(order = 1) val permission = notificationPermission()
    @get:Rule(order = 2) val compose = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun shownTime(prefix: String): String =
        compose.onNodeWithText(prefix, substring = true).fetchSemanticsNode()
            .config[SemanticsProperties.Text].first().text

    @Test fun timerStartsAtTheChosenDuration() {
        compose.planSession("30", "Cálculo I")
        compose.onNodeWithText("30:00").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Toque em Começar quando estiver pronto.").assertExists()
        compose.onNodeWithText("Pausar").assertDoesNotExist()
    }

    @Test fun startPauseContinueAndReset() {
        compose.planSession("30", "Cálculo I")
        compose.tap("Começar")
        compose.waitForText("Cronômetro rodando.")
        Thread.sleep(1_500)
        compose.tap("Pausar")
        compose.waitForText("Cronômetro pausado.")
        val paused = shownTime("29:5")
        // Pausado, o tempo não corre.
        Thread.sleep(1_500)
        compose.waitForIdle()
        compose.onNodeWithText(paused).assertExists()

        compose.tap("Continuar")
        compose.waitForText("Cronômetro rodando.")
        compose.onAllNodesWithText("Recomeçar")[0].performClick()
        compose.waitForText("30:00")
        compose.onNodeWithText("Começar").assertExists()
    }

    @Test fun timerKeepsRunningWhenLeavingTheTab() {
        compose.planSession("30", "Cálculo I")
        compose.tap("Começar")
        compose.openTab("Histórico")
        Thread.sleep(2_500)
        compose.openTab("Planejar")
        compose.openTab("Cronômetro")
        compose.waitForText("Cronômetro rodando.")
        compose.onNodeWithText("Pausar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("29:5", substring = true).assertExists() // andou enquanto estava em outra aba
    }

    @Test fun runningTimerSurvivesRecreation() {
        compose.planSession("45", "Cálculo I")
        compose.tap("Começar")
        Thread.sleep(1_500)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Pausar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("44:5", substring = true).assertExists()
    }

    @Test fun runningTimerIsSavedOutsideTheScreen() {
        compose.planSession("30", "Cálculo I")
        compose.tap("Começar")
        val before = SessionClock.state.value!!.timer
        // Como se o processo do app fosse encerrado: o estado em memória some e é lido de novo do aparelho.
        SessionClock.forgetInMemoryState()
        SessionClock.init(context)
        val after = SessionClock.state.value!!
        assertTrue(after.timer.running)
        assertEquals(before.endAt, after.timer.endAt)
        assertEquals("Cálculo I", after.subject)
    }
}
