package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import org.junit.Rule
import org.junit.Test

class DurationTest {
    @get:Rule(order = 0) val clear = ClearDatabaseRule()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    private fun typeMinutes(text: String) {
        compose.onNodeWithText("Minutos").performScrollTo().performTextReplacement(text)
        closeSoftKeyboard()
    }

    private fun openPlanWithSubject() {
        compose.onNodeWithText("Assunto").performScrollTo().performTextReplacement("Cálculo I")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
    }

    @Test fun shortcutsFillTheFieldAndUpdateTheSummary() {
        compose.onNodeWithText("25 min").performScrollTo().assertIsSelected()
        compose.onNodeWithText("Duração escolhida: 25 minutos.").assertExists()
        repeat(2) {
            listOf(15, 45, 60, 25).forEach { minutes ->
                compose.onNodeWithText("$minutes min").performScrollTo().performClick().assertIsSelected()
                listOf(15, 25, 45, 60).filter { it != minutes }.forEach { other ->
                    compose.onNodeWithText("$other min").assertIsNotSelected()
                }
                compose.onNodeWithText("Duração escolhida: $minutes minutos.").assertExists()
            }
        }
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().assertIsDisplayed()
    }

    @Test fun typedDurationIsUsedInThePlan() {
        typeMinutes("50")
        listOf(15, 25, 45, 60).forEach { compose.onNodeWithText("$it min").assertIsNotSelected() }
        compose.onNodeWithText("Duração escolhida: 50 minutos.").assertExists()
        openPlanWithSubject()
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        compose.onNodeWithText("50 minutos").assertIsDisplayed()
    }

    @Test fun fieldAcceptsOnlyDigits() {
        typeMinutes("3a0")
        compose.onNodeWithText("Minutos").assert(hasText("25"))
    }

    @Test fun durationOutOfRangeShowsErrorAndStaysOnHome() {
        typeMinutes("3")
        openPlanWithSubject()
        compose.onNodeWithText("Use pelo menos 5 minutos.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()
        typeMinutes("200")
        compose.onNodeWithText("Use no máximo 180 minutos.").performScrollTo().assertIsDisplayed()
        typeMinutes("")
        compose.onNodeWithText("Informe quantos minutos você vai estudar.").performScrollTo().assertIsDisplayed()
        typeMinutes("90")
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("90 minutos").assertIsDisplayed()
    }

    @Test fun typedDurationSurvivesActivityRecreation() {
        typeMinutes("35")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Duração escolhida: 35 minutos.").performScrollTo().assertExists()
    }
}
