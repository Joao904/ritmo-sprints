package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import br.unemat.ritmo.data.RitmoDatabase
import br.unemat.ritmo.data.StudySession
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Gera timer-running.png, weekly-goal.png, history-all.png e history-deleted.png. */
class ImprovementsEvidence {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun onlyWhenRequested() = Evidence.requireEvidenceRun()

    @Test fun timer() {
        compose.onNodeWithText("Minutos").performScrollTo().performTextReplacement("50")
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Cálculo I — limites")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Começar").performScrollTo().performClick()
        Thread.sleep(3_000)
        compose.onNodeWithText("Pausar").performScrollTo()
        Evidence.screenshot("timer-running")
    }

    @Test fun history() {
        ClearDatabaseRule.clear()
        val dao = RitmoDatabase.get(InstrumentationRegistry.getInstrumentation().targetContext).sessionDao()
        val now = System.currentTimeMillis()
        val minute = 60_000L
        // Sessões de hoje, separadas por alguns minutos, para todas contarem na meta da semana.
        runBlocking {
            listOf(
                "Anatomia — ossos do crânio" to 30,
                "Inglês — phrasal verbs" to 20,
                "Física II — leis de Newton" to 45,
                "Química orgânica" to 25,
                "Estatística — média e moda" to 40,
                "Cálculo I — limites" to 50,
            ).forEachIndexed { index, (subject, minutes) ->
                dao.insert(StudySession(subject = subject, goal = null, minutes = minutes, createdAt = now - (6 - index) * 10 * minute))
            }
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("6 sessões salvas", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Seu histórico").performScrollTo()
        compose.onNodeWithText("META DA SEMANA").performScrollTo()
        Evidence.screenshot("weekly-goal")

        compose.onNodeWithText("Ver todas as 6 sessões").performScrollTo().performClick()
        compose.onNodeWithText("Mostrar menos").performScrollTo()
        Evidence.screenshot("history-all")

        compose.onNodeWithContentDescription("Excluir Química orgânica").performScrollTo().performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Desfazer").fetchSemanticsNodes().isNotEmpty() }
        Evidence.screenshot("history-deleted")
    }
}
