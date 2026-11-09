package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class FormTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun tapPlan() =
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()

    private fun typeSubject(text: String) {
        compose.onNodeWithText("Assunto").performScrollTo().performTextInput(text)
        closeSoftKeyboard()
    }

    @Test fun emptySubjectShowsErrorAndStaysOnHome() {
        tapPlan()
        compose.onNodeWithText("Informe o assunto que você vai estudar.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()
    }

    @Test fun shortSubjectShowsMinimumError() {
        typeSubject("ab")
        tapPlan()
        compose.onNodeWithText("Use pelo menos 3 caracteres.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()
    }

    @Test fun longSubjectAndGoalShowMaximumErrors() {
        typeSubject("a".repeat(41))
        compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextInput("b".repeat(81))
        tapPlan()
        compose.onNodeWithText("Use no máximo 40 caracteres.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Use no máximo 80 caracteres.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Seu plano").assertDoesNotExist()
    }

    @Test fun errorDisappearsWhenInputIsCorrected() {
        tapPlan()
        compose.onNodeWithText("Informe o assunto que você vai estudar.").assertExists()
        typeSubject("Cálculo I")
        compose.onNodeWithText("Informe o assunto que você vai estudar.").assertDoesNotExist()
        tapPlan()
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
    }

    @Test fun validInputOpensPlanWithSubjectAndGoal() {
        compose.onNodeWithText("45 min").performScrollTo().performClick()
        typeSubject("  Cálculo I  ")
        compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextInput("resolver 5 exercícios")
        tapPlan()
        compose.onNodeWithText("Seu plano").assertIsDisplayed()
        compose.onNodeWithText("45 minutos").assertIsDisplayed()
        compose.onNodeWithText("Assunto: Cálculo I").assertIsDisplayed()
        compose.onNodeWithText("Objetivo: resolver 5 exercícios").assertIsDisplayed()
    }

    @Test fun validInputWithoutGoalIsAccepted() {
        typeSubject("Anatomia")
        tapPlan()
        compose.onNodeWithText("Assunto: Anatomia").assertIsDisplayed()
        compose.onNodeWithText("Objetivo:", substring = true).assertDoesNotExist()
    }

    @Test fun typedValuesSurviveBackNavigationAndRecreation() {
        typeSubject("Cálculo I")
        tapPlan()
        pressBack()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Cálculo I").performScrollTo().assertIsDisplayed()
    }
}
