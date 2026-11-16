package br.unemat.ritmo

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import org.junit.Rule
import org.junit.Test

/** Cronômetro na tela do plano. */
class TimerTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun openPlan(minutes: String) {
        compose.onNodeWithText("Minutos").performScrollTo().performTextReplacement(minutes)
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Cálculo I")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
    }

    private fun shownTime(prefix: String): String =
        compose.onNodeWithText(prefix, substring = true).fetchSemanticsNode()
            .config[SemanticsProperties.Text].first().text

    @Test fun timerStartsAtTheChosenDuration() {
        openPlan("30")
        compose.onNodeWithText("30:00").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Toque em Começar quando estiver pronto.").assertExists()
        compose.onNodeWithText("Pausar").assertDoesNotExist()
    }

    @Test fun startPauseContinueAndReset() {
        openPlan("30")
        compose.onNodeWithText("Começar").performScrollTo().performClick()
        compose.onNodeWithText("Cronômetro rodando.").assertExists()
        Thread.sleep(1_500)
        compose.onNodeWithText("Pausar").performClick()
        compose.onNodeWithText("Cronômetro pausado.").assertExists()
        val paused = shownTime("29:5")
        // Pausado, o tempo não corre.
        Thread.sleep(1_500)
        compose.waitForIdle()
        compose.onNodeWithText(paused).assertExists()

        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("Cronômetro rodando.").assertExists()
        compose.onNodeWithText("Recomeçar").performClick()
        compose.onNodeWithText("30:00").assertExists()
        compose.onNodeWithText("Começar").assertExists()
    }

    @Test fun runningTimerSurvivesRecreation() {
        openPlan("45")
        compose.onNodeWithText("Começar").performScrollTo().performClick()
        Thread.sleep(1_500)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Pausar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("44:5", substring = true).assertExists()
    }
}
