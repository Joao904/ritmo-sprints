package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Gera invalid-input.png, valid-form.png e valid-input.png da Sprint 04. */
class Sprint04Evidence {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun onlyWhenRequested() = Evidence.requireEvidenceRun()

    @Test fun invalidInput() {
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().performClick()
        compose.onNodeWithText("Informe o assunto que você vai estudar.").assertExists()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo()
        Evidence.screenshot("invalid-input")
    }

    @Test fun validInput() {
        compose.onNodeWithText("45 min").performScrollTo().performClick()
        compose.onNodeWithText("Assunto").performScrollTo().performTextInput("Cálculo I — limites")
        compose.onNodeWithText("Objetivo (opcional)").performScrollTo().performTextInput("resolver 5 exercícios")
        closeSoftKeyboard()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo()
        Evidence.screenshot("valid-form")
        compose.onNodeWithText("Planejar meu estudo").performClick()
        compose.onNodeWithText("Assunto: Cálculo I — limites").assertIsDisplayed()
        Evidence.screenshot("valid-input")
    }
}
