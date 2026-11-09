package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class DurationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun defaultAndRepeatedSelectionsUpdateExactlyOneOptionAndSummary() {
        compose.onNodeWithText("25 min").performScrollTo().assertIsSelected()
        compose.onNodeWithText("Duração escolhida: 25 minutos.").assertExists()
        repeat(3) {
            listOf(15, 45, 25).forEach { minutes ->
                compose.onNodeWithText("$minutes min").performScrollTo().performClick().assertIsSelected()
                listOf(15, 25, 45).filter { it != minutes }.forEach { other ->
                    compose.onNodeWithText("$other min").assertIsNotSelected()
                }
                compose.onNodeWithText("Duração escolhida: $minutes minutos.").assertExists()
            }
        }
        compose.onNodeWithText("Ritmo").assertExists()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().assertIsDisplayed()
    }

    @Test fun selectionSurvivesActivityRecreation() {
        compose.onNodeWithText("45 min").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("45 min").performScrollTo().assertIsSelected()
        compose.onNodeWithText("Duração escolhida: 45 minutos.").assertExists()
    }
}
