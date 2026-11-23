package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import br.unemat.ritmo.data.RitmoDatabase
import br.unemat.ritmo.data.StudySession
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

/** Sprint 07: salvar leva à aba Histórico, onde ficam a meta da semana e as sessões. */
class HistoryTest {
    private val compose = createAndroidComposeRule<MainActivity>()

    // The database is cleared before the activity starts.
    @get:Rule val rules: RuleChain = RuleChain.outerRule(ClearDatabaseRule()).around(compose)

    private fun waitForText(text: String, substring: Boolean = false) =
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text, substring).fetchSemanticsNodes().isNotEmpty() }

    private fun planAndSave(minutes: Int, subject: String, goal: String = "") {
        compose.onNodeWithText("$minutes min").performScrollTo().performClick()
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement(subject)
        if (goal.isNotEmpty()) {
            compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextReplacement(goal)
        }
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Salvar no histórico").performScrollTo().performClick()
    }

    /** A mensagem "Sessão salva" cobre o fim da tela por alguns segundos: espera ela sumir antes de tocar lá. */
    private fun waitForSavedMessageToClose() {
        waitForText("Sessão salva no histórico.")
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Sessão salva no histórico.").fetchSemanticsNodes().isEmpty() }
    }

    @Test fun emptyHistoryShowsHint() {
        compose.openTab("Histórico")
        compose.onNodeWithText("Seu histórico").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Nenhuma sessão salva ainda.", substring = true).assertExists()
    }

    @Test fun savingAddsSessionToHistoryAndClearsTheForm() {
        planAndSave(45, "Bioquímica", "revisar enzimas")
        waitForText("1 sessão salva · 45 minutos no total")
        compose.onNodeWithText("Bioquímica").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Objetivo: revisar enzimas").assertExists()
        compose.onNodeWithText("45 min ·", substring = true).assertExists()
        // Form cleared: the subject only appears once (in the history), not in the text field.
        compose.onAllNodesWithText("Bioquímica").assertCountEquals(1)
    }

    @Test fun sessionWithoutGoalIsSavedWithoutGoalLine() {
        planAndSave(15, "Física II")
        waitForText("Física II")
        compose.onNodeWithText("Objetivo:", substring = true).assertDoesNotExist()
    }

    @Test fun doubleTapOnSaveStoresOneSession() {
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Bioquímica")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Salvar no histórico").performScrollTo().performTouchInput { doubleClick() }
        waitForText("1 sessão salva · 25 minutos no total")
        Thread.sleep(1_000)
        compose.onNodeWithText("1 sessão salva · 25 minutos no total").assertExists()
    }

    @Test fun deleteRemovesTheSessionAndUndoBringsItBack() {
        planAndSave(45, "Bioquímica")
        waitForSavedMessageToClose()
        compose.onNodeWithContentDescription("Excluir Bioquímica").performScrollTo().performClick()
        waitForText("Nenhuma sessão salva", substring = true)
        compose.onNodeWithText("Bioquímica").assertDoesNotExist()
        compose.onNodeWithText("Desfazer").performClick()
        waitForText("1 sessão salva · 45 minutos no total")
        compose.onNodeWithText("Bioquímica").performScrollTo().assertIsDisplayed()
    }

    @Test fun weeklyGoalCountsSavedMinutesAndCanBeChanged() {
        compose.openTab("Histórico")
        compose.onNodeWithText("0 min de 5 h").performScrollTo().assertIsDisplayed()
        compose.openTab("Planejar")
        planAndSave(45, "Bioquímica")
        waitForText("45 min de 5 h")
        waitForSavedMessageToClose()
        compose.onNodeWithText("Faltam 4 h 15 min nesta semana.").assertExists()
        compose.onNodeWithText("+ 30 min").performScrollTo().performClick()
        compose.onNodeWithText("45 min de 5 h 30 min").assertExists()
        compose.onNodeWithText("− 30 min").performClick()
        compose.onNodeWithText("− 30 min").performClick()
        compose.onNodeWithText("45 min de 4 h 30 min").assertExists()
        // A meta fica guardada: continua depois de recriar a tela.
        compose.activityRule.scenario.recreate()
        waitForText("45 min de 4 h 30 min")
    }

    @Test fun homeReadsDatabaseNewestFirstLimitedToFive() {
        val dao = RitmoDatabase.get(InstrumentationRegistry.getInstrumentation().targetContext).sessionDao()
        runBlocking {
            (1..6).forEach { n ->
                dao.insert(StudySession(subject = "Tema $n", goal = null, minutes = 15, createdAt = n * 60_000L))
            }
        }
        compose.openTab("Histórico")
        waitForText("6 sessões salvas · 90 minutos no total")
        compose.onNodeWithText("Tema 6").assertExists()
        compose.onNodeWithText("Tema 2").assertExists()
        compose.onNodeWithText("Tema 1").assertDoesNotExist()
        // Ver todas mostra também as mais antigas; Mostrar menos volta às 5.
        compose.onNodeWithText("Ver todas as 6 sessões").performScrollTo().performClick()
        compose.onNodeWithText("Tema 1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Mostrar menos").performScrollTo().performClick()
        compose.onNodeWithText("Tema 1").assertDoesNotExist()
    }
}
