package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

/** Sprint 03, refeito na Sprint 07: navegação pelas abas Planejar, Cronômetro e Histórico. */
class NavigationTest {
    @get:Rule(order = 0) val clear = ClearDatabaseRule()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test fun planOpensTheTimerTabWithTheChosenDuration() {
        listOf(15, 25, 45).forEach { minutes ->
            compose.tap("$minutes min")
            compose.type("Assunto", "Cálculo I")
            compose.tap("Planejar meu estudo")
            compose.onNodeWithText("Seu plano").assertIsDisplayed()
            compose.onNodeWithText("$minutes minutos").assertIsDisplayed()
            listOf("Prepare seu espaço", "Concentre-se", "Faça uma pausa").forEach { title ->
                compose.onNodeWithText(title).performScrollTo().assertIsDisplayed()
            }
            compose.tap("Ajustar duração")
            compose.onNodeWithText("$minutes min").performScrollTo().assertIsSelected()
        }
    }

    @Test fun tabsSwitchAndKeepEachScreen() {
        compose.planSession("45", "Cálculo I")
        compose.openTab("Histórico")
        compose.onNodeWithText("Seu progresso").assertIsDisplayed()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()
        compose.openTab("Cronômetro")
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        compose.onNodeWithText("45 minutos").assertIsDisplayed()
        compose.openTab("Planejar")
        compose.onNodeWithText("Duração escolhida: 45 minutos.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Cálculo I").performScrollTo().assertIsDisplayed()
    }

    @Test fun androidBackFromAnyTabReturnsToPlan() {
        compose.planSession("15", "Cálculo I")
        pressBack()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("15 min").assertIsSelected()
        compose.openTab("Histórico")
        pressBack()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().assertIsDisplayed()
    }

    @Test fun timerTabWithoutSessionExplainsWhatToDo() {
        compose.openTab("Cronômetro")
        compose.onNodeWithText("Nenhuma sessão planejada.").assertIsDisplayed()
        compose.tap("Ir para Planejar")
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().assertIsDisplayed()
    }

    @Test fun rapidForwardTapsDoNotDuplicateTheDestination() {
        compose.type("Assunto", "Cálculo I")
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performTouchInput { doubleClick() }
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("25 min").performScrollTo().assertIsSelected()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()
    }

    @Test fun recreationKeepsTabAndSession() {
        compose.planSession("45", "Cálculo I")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        compose.onNodeWithText("45 minutos").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("45 min").performScrollTo().assertIsSelected()
    }
}
