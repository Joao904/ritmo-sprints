package br.unemat.ritmo

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import br.unemat.ritmo.reminder.SessionReminder
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

/**
 * Sprint 07 — teste de regressão do app inteiro, na ordem do roteiro da SPEC-007 (RS-01 a RS-09).
 * Começa com o banco vazio e percorre, em uma única jornada, tudo o que foi entregue nas sprints 01 a 06.
 */
class FinalRegressionTest {
    private val compose = createAndroidComposeRule<MainActivity>()
    private val permission: TestRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        else TestRule { base, _ -> base }

    @get:Rule val rules: RuleChain = RuleChain.outerRule(ClearDatabaseRule()).around(permission).around(compose)

    @After fun cancelReminder() = SessionReminder.cancel(InstrumentationRegistry.getInstrumentation().targetContext)

    private fun waitForText(text: String, substring: Boolean = false) =
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text, substring).fetchSemanticsNodes().isNotEmpty() }

    private fun tap(text: String) = compose.onNodeWithText(text).performScrollTo().performClick()

    private fun type(label: String, value: String) {
        compose.onNodeWithText(label).performScrollTo().performTextReplacement(value)
        closeSoftKeyboard()
    }

    @Test fun completeJourneyFromLaunch() {
        // RS-01 — o app abre na tela inicial (SPEC-001).
        compose.onNodeWithText("Ritmo").assertIsDisplayed()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().assertIsDisplayed()

        // RS-02 — tela inicial utilizável, com estado vazio explicado (SPEC-005).
        compose.onNodeWithText("Nenhuma sessão salva ainda.", substring = true).performScrollTo().assertIsDisplayed()

        // RS-03 — estado e interação: o atalho muda a duração e o resumo na hora (SPEC-002/006).
        tap("45 min")
        compose.onNodeWithText("45 min").assertIsSelected()
        compose.onNodeWithText("Duração escolhida: 45 minutos.").assertExists()

        // RS-04 — validação: dados inválidos mostram as mensagens e não abrem o plano (SPEC-004/006).
        type("Minutos", "2")
        tap("Planejar meu estudo")
        compose.onNodeWithText("Use pelo menos 5 minutos.").assertExists()
        compose.onNodeWithText("Informe o assunto que você vai estudar.").assertExists()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()

        // Dados válidos abrem o plano (SPEC-003).
        type("Minutos", "40")
        type("Assunto", "Cálculo I — limites")
        type("Objetivo (opcional)", "resolver 5 exercícios")
        tap("Planejar meu estudo")
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        compose.onNodeWithText("40 minutos").assertIsDisplayed()
        compose.onNodeWithText("Assunto: Cálculo I — limites").assertIsDisplayed()
        compose.onNodeWithText("Objetivo: resolver 5 exercícios").assertIsDisplayed()

        // RS-07 — integração: cronômetro e aviso de fim juntos (SPEC-006).
        tap("Começar")
        waitForText("Cronômetro rodando.")
        tap("Avisar quando acabar")
        waitForText("Aviso marcado para", substring = true)
        tap("Pausar")
        waitForText("Aviso pausado junto com o cronômetro.")
        tap("Continuar")
        waitForText("Aviso marcado para", substring = true)
        tap("Cancelar aviso")
        compose.onNodeWithText("Avisar quando acabar").performScrollTo().assertIsDisplayed()

        // RS-08 — voltar: o botão Voltar do Android retorna à tela inicial com os dados preservados.
        pressBack()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()
        compose.onNodeWithText("Duração escolhida: 40 minutos.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Cálculo I — limites").assertExists() // ainda no campo Assunto

        // RS-05/06 — salvar: a sessão vai para o banco, o formulário é limpo e a meta da semana soma os minutos.
        tap("Planejar meu estudo")
        tap("Salvar no histórico")
        waitForText("Sessão salva no histórico.")
        waitForText("1 sessão salva · 40 minutos no total")
        compose.onNodeWithText("Cálculo I — limites").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Objetivo: resolver 5 exercícios").assertExists()
        compose.onNodeWithText("40 min de 5 h").performScrollTo().assertIsDisplayed()

        // A tela é recriada (como ao girar o aparelho) e os dados continuam lá.
        compose.activityRule.scenario.recreate()
        waitForText("1 sessão salva · 40 minutos no total")
        compose.onNodeWithText("Cálculo I — limites").performScrollTo().assertIsDisplayed()

        // RS-09 — chegar aqui sem exceção significa que a jornada terminou sem travar o app.
    }
}
