package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun openPlan(minutes: Int) {
        compose.onNodeWithText("$minutes min").performScrollTo().performClick()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        compose.onNodeWithText("$minutes minutos").assertIsDisplayed()
    }

    @Test fun allDurationsReachThePlanAndAdjustReturnsWithSelection() {
        listOf(15, 25, 45).forEach { minutes ->
            openPlan(minutes)
            listOf("Prepare seu espaço", "Concentre-se", "Faça uma pausa").forEach { title ->
                compose.onNodeWithText(title).performScrollTo().assertIsDisplayed()
            }
            compose.onNodeWithText("Ajustar duração").performScrollTo().performClick()
            compose.onNodeWithText("$minutes min").performScrollTo().assertIsSelected()
        }
    }

    @Test fun toolbarAndAndroidBackReturnToThePreviousScreen() {
        openPlan(45)
        compose.onNodeWithText("Voltar").performClick()
        compose.onNodeWithText("45 min").assertIsSelected()
        openPlan(15)
        pressBack()
        compose.onNodeWithText("15 min").assertIsSelected()
        compose.onNodeWithText("Duração escolhida: 15 minutos.").assertExists()
    }

    @Test fun rapidForwardTapsDoNotDuplicateTheDestination() {
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performTouchInput { doubleClick() }
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("25 min").assertIsSelected()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()
    }

    @Test fun recreationOnPlanKeepsDestinationAndDuration() {
        openPlan(45)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        compose.onNodeWithText("45 minutos").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("45 min").performScrollTo().assertIsSelected()
    }
}
