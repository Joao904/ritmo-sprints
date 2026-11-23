package br.unemat.ritmo

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.rule.GrantPermissionRule
import org.junit.rules.TestRule

/** Passos repetidos pelos testes (Sprint 07: o app tem as abas Planejar, Cronômetro e Histórico). */

fun ComposeTestRule.type(label: String, value: String) {
    onNodeWithText(label).performScrollTo().performTextReplacement(value)
    closeSoftKeyboard()
}

fun ComposeTestRule.tap(text: String) = onNodeWithText(text).performScrollTo().performClick()

/** Toca em uma aba da barra inferior. */
fun ComposeTestRule.openTab(label: String) {
    onNodeWithText(label).performClick()
    waitForIdle()
}

fun ComposeTestRule.waitForText(text: String, substring: Boolean = false, timeoutMs: Long = 10_000) =
    waitUntil(timeoutMs) { onAllNodesWithText(text, substring).fetchSemanticsNodes().isNotEmpty() }

/** Preenche a aba Planejar e toca em Planejar meu estudo (abre a aba Cronômetro). */
fun ComposeTestRule.planSession(minutes: String, subject: String, goal: String = "") {
    type("Minutos", minutes)
    type("Assunto", subject)
    if (goal.isNotEmpty()) type("Objetivo (opcional)", goal)
    tap("Planejar meu estudo")
    onNodeWithText("Seu plano").assertIsDisplayed()
}

/** Concede a permissão de notificação (só existe em tempo de execução a partir do Android 13). */
fun notificationPermission(): TestRule =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    else TestRule { base, _ -> base }
