package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Gera data-created.png e data-restored.png da Sprint 05.
 * O script executa createData, encerra o processo do app e só então executa restoreData,
 * que abre o app do zero e lê o histórico salvo no banco.
 */
class Sprint05Evidence {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun onlyWhenRequested() = Evidence.requireEvidenceRun()

    private fun waitForText(text: String) =
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    private fun planAndSave(minutes: Int, subject: String, goal: String) {
        compose.onNodeWithText("$minutes min").performScrollTo().performClick()
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement(subject)
        compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextReplacement(goal)
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Salvar no histórico").performScrollTo().performClick()
    }

    @Test fun createData() {
        ClearDatabaseRule.clear()
        planAndSave(25, "Anatomia", "")
        waitForText("1 sessão salva · 25 minutos no total")
        waitForSnackbarToHide()
        planAndSave(45, "Cálculo I — limites", "resolver 5 exercícios")
        waitForText("2 sessões salvas · 70 minutos no total")
        waitForSnackbarToHide()
        scrollToBottom()
        Evidence.screenshot("data-created")
    }

    @Test fun restoreData() {
        waitForText("2 sessões salvas · 70 minutos no total")
        compose.onNodeWithText("Cálculo I — limites").assertExists()
        compose.onNodeWithText("Anatomia").assertExists()
        scrollToBottom()
        Evidence.screenshot("data-restored")
    }

    // The confirmation snackbar covers the bottom of the screen; wait for it to go away.
    private fun waitForSnackbarToHide() = compose.waitUntil(10_000) {
        compose.onAllNodesWithText("Sessão salva no histórico.").fetchSemanticsNodes().isEmpty()
    }

    // Shows the whole history (both sessions) in the capture.
    private fun scrollToBottom() {
        repeat(3) { compose.onNode(hasScrollAction()).performTouchInput { swipeUp() } }
        compose.waitForIdle()
    }
}
