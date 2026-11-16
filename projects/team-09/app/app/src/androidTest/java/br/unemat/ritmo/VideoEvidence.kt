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

/** Telas do vídeo de divulgação, em sequência: início, formulário, plano, cronômetro e histórico. */
class VideoEvidence {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun onlyWhenRequested() = Evidence.requireEvidenceRun()

    @Test fun journey() {
        ClearDatabaseRule.clear()
        val dao = RitmoDatabase.get(InstrumentationRegistry.getInstrumentation().targetContext).sessionDao()
        val now = System.currentTimeMillis()
        runBlocking {
            dao.insert(StudySession(subject = "Anatomia — ossos do crânio", goal = null, minutes = 30, createdAt = now - 26 * 3_600_000L))
            dao.insert(StudySession(subject = "Física II — leis de Newton", goal = "fazer a lista 3", minutes = 50, createdAt = now - 3 * 3_600_000L))
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("2 sessões salvas", substring = true).fetchSemanticsNodes().isNotEmpty() }
        Evidence.screenshot("video-1-inicio")

        compose.onNodeWithText("Minutos").performScrollTo().performTextReplacement("45")
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Cálculo I — limites")
        compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextReplacement("resolver 5 exercícios")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo()
        Evidence.screenshot("video-2-formulario")

        compose.onNodeWithText("Planejar meu estudo").performClick()
        compose.onNodeWithText("Começar").performScrollTo()
        Evidence.screenshot("video-3-plano")

        compose.onNodeWithText("Começar").performClick()
        Thread.sleep(3_000)
        compose.onNodeWithText("Pausar").performScrollTo()
        Evidence.screenshot("video-4-cronometro")

        compose.onNodeWithText("Salvar no histórico").performScrollTo().performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("3 sessões salvas", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Sessão salva no histórico.").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Seu histórico").performScrollTo()
        compose.onNodeWithText("META DA SEMANA").performScrollTo()
        Evidence.screenshot("video-5-historico")
    }
}
