package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import br.unemat.ritmo.timer.AlarmScheduler
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Sprint 07 — teste de regressão do app inteiro, na ordem do roteiro da SPEC-007 (RS-01 a RS-09).
 * Começa sem dados e percorre, em uma única jornada, tudo o que foi entregue nas sprints 01 a 07.
 */
class FinalRegressionTest {
    @get:Rule(order = 0) val clear = ClearDatabaseRule()
    @get:Rule(order = 1) val permission = notificationPermission()
    @get:Rule(order = 2) val compose = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun completeJourneyFromLaunch() {
        // RS-01 — o app abre na aba Planejar (SPEC-001).
        compose.onNodeWithText("Ritmo").assertIsDisplayed()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().assertIsDisplayed()

        // RS-02 — tela utilizável; as três abas aparecem; o histórico vazio explica o que fazer (SPEC-005).
        listOf("Planejar", "Cronômetro", "Histórico").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        compose.openTab("Histórico")
        compose.onNodeWithText("Nenhuma sessão salva ainda.", substring = true).performScrollTo().assertIsDisplayed()
        compose.openTab("Planejar")

        // RS-03 — estado e interação: o atalho muda a duração e o resumo na hora (SPEC-002/006).
        compose.tap("45 min")
        compose.onNodeWithText("45 min").assertIsSelected()
        compose.onNodeWithText("Duração escolhida: 45 minutos.").assertExists()

        // RS-04 — validação: dados inválidos mostram as mensagens e não abrem o plano (SPEC-004/006).
        compose.type("Minutos", "2")
        compose.tap("Planejar meu estudo")
        compose.onNodeWithText("Use pelo menos 5 minutos.").assertExists()
        compose.onNodeWithText("Informe o assunto que você vai estudar.").assertExists()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()

        // RS-05 — dados válidos abrem a aba Cronômetro com o plano (SPEC-003).
        compose.planSession("40", "Cálculo I — limites", "resolver 5 exercícios")
        compose.onNodeWithText("40 minutos").assertIsDisplayed()
        compose.onNodeWithText("Assunto: Cálculo I — limites").assertIsDisplayed()
        compose.onNodeWithText("Objetivo: resolver 5 exercícios").assertIsDisplayed()

        // RS-07 — cronômetro e alarme: Começar marca o alarme sozinho; trocar de aba não para o cronômetro.
        compose.tap("Começar")
        compose.waitForText("Alarme marcado para", substring = true)
        assertTrue(AlarmScheduler.isScheduled(context))
        compose.openTab("Histórico")
        Thread.sleep(2_000)
        compose.openTab("Cronômetro")
        compose.waitForText("Cronômetro rodando.")
        compose.onNodeWithText("39:5", substring = true).assertExists()
        compose.tap("Pausar")
        compose.waitForText("Alarme pausado junto com o cronômetro.")
        assertFalse(AlarmScheduler.isScheduled(context))
        compose.tap("Continuar")
        compose.waitForText("Alarme marcado para", substring = true)

        // RS-08 — voltar: o Voltar do Android leva à aba Planejar com os dados preservados; o cronômetro segue rodando.
        pressBack()
        compose.onNodeWithText("Duração escolhida: 40 minutos.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Cálculo I — limites").performScrollTo().assertExists() // ainda no campo Assunto
        assertTrue(AlarmScheduler.isScheduled(context))

        // RS-06 — salvar: a sessão vai para o banco, a aba Histórico abre e a meta da semana soma os minutos.
        compose.openTab("Cronômetro")
        compose.tap("Salvar no histórico")
        compose.waitForText("1 sessão salva · 40 minutos no total")
        compose.onNodeWithText("Cálculo I — limites").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Objetivo: resolver 5 exercícios").assertExists()
        compose.onNodeWithText("40 min de 5 h").performScrollTo().assertIsDisplayed()
        assertFalse("salvar encerra o alarme", AlarmScheduler.isScheduled(context))

        // A tela é recriada (como ao girar o aparelho) e os dados continuam lá.
        compose.activityRule.scenario.recreate()
        compose.waitForText("1 sessão salva · 40 minutos no total")
        compose.onNodeWithText("Cálculo I — limites").performScrollTo().assertIsDisplayed()

        // RS-09 — chegar aqui sem exceção significa que a jornada terminou sem travar o app.
    }
}
