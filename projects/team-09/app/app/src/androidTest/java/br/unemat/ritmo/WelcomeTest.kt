package br.unemat.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class WelcomeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun presentationAndPrimaryActionAreVisible() {
        compose.onNodeWithText("Ritmo").assertIsDisplayed()
        compose.onNodeWithText("Estude no\nseu ritmo.").assertIsDisplayed()
        compose.onNodeWithText("Uma sessão de cada vez.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Planejar meu estudo").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ritmo").assertExists()
    }
}
