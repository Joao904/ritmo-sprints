package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import br.unemat.ritmo.data.RitmoDatabase
import br.unemat.ritmo.data.StudySession
import br.unemat.ritmo.reminder.SessionReminder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.regex.Pattern

/**
 * Gera final-home.png, final-feature.png e final-history.png.
 * O script de validação limpa os dados do app antes do primeiro passo e retira a permissão
 * de notificação antes de cada passo, então a janela de permissão do sistema aparece de verdade.
 */
class Sprint07Evidence {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun onlyWhenRequested() = Evidence.requireEvidenceRun()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    private fun waitForText(text: String, substring: Boolean = false) =
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text, substring).fetchSemanticsNodes().isNotEmpty() }

    /** Tela inicial logo depois de abrir o app, sem nenhum dado. */
    @Test fun finalHome() {
        compose.onNodeWithText("Planejar meu estudo").assertExists()
        Evidence.screenshot("final-home")
    }

    /** Função principal: plano com cronômetro rodando e aviso de fim marcado (permissão dada na janela do sistema). */
    @Test fun finalFeature() {
        compose.onNodeWithText("Minutos").performScrollTo().performTextReplacement("50")
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Cálculo I — limites")
        compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextReplacement("resolver 5 exercícios")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Começar").performScrollTo().performClick()
        compose.onNodeWithText("Avisar quando acabar").performScrollTo().performClick()

        val device = UiDevice.getInstance(instrumentation)
        val allow = device.wait(Until.findObject(By.res("com.android.permissioncontroller", "permission_allow_button")), 10_000)
            ?: device.wait(Until.findObject(By.text(Pattern.compile("(?i)^allow$|^permitir$"))), 5_000)
        assertNotNull("janela de permissão não apareceu", allow)
        allow.click()

        waitForText("Aviso marcado para", substring = true)
        Thread.sleep(2_000) // deixa o cronômetro andar alguns segundos
        compose.onNodeWithText("Cancelar aviso").performScrollTo()
        Evidence.screenshot("final-feature")
        SessionReminder.cancel(instrumentation.targetContext)
    }

    /** Histórico e meta da semana: 4 sessões anteriores gravadas no banco e 1 salva pela interface. */
    @Test fun finalHistory() {
        ClearDatabaseRule.clear()
        val dao = RitmoDatabase.get(instrumentation.targetContext).sessionDao()
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
        waitForText("4 sessões salvas", substring = true)

        compose.onNodeWithText("Minutos").performScrollTo().performTextReplacement("50")
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Cálculo I — limites")
        compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextReplacement("resolver 5 exercícios")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Salvar no histórico").performScrollTo().performClick()
        waitForText("5 sessões salvas", substring = true)
        // Espera a mensagem "Sessão salva no histórico." sumir para ela não cobrir a lista.
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Sessão salva no histórico.").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("META DA SEMANA").performScrollTo()
        compose.onNodeWithText("Seu histórico").performScrollTo()
        Evidence.screenshot("final-history")
    }
}
