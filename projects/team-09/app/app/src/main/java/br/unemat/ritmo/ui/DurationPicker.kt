package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.unemat.ritmo.model.SessionRules

/**
 * Duração da sessão (Sprint 06): o estudante digita quantos minutos tem.
 * Os atalhos só preenchem o campo com valores comuns.
 * O campo aceita apenas dígitos (até 3); o intervalo é conferido por SessionRules.minutesError.
 */
@Composable
fun DurationPicker(minutesText: String, showErrors: Boolean, onMinutesChange: (String) -> Unit) {
    val error = if (showErrors) SessionRules.minutesError(minutesText) else null
    val validMinutes = minutesText.toIntOrNull()?.takeIf { SessionRules.minutesError(minutesText) == null }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quanto tempo você tem?", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = minutesText,
            onValueChange = { new -> if (new.length <= 3 && new.all(Char::isDigit)) onMinutesChange(new) },
            label = { Text("Minutos") },
            suffix = { Text("min") },
            singleLine = true,
            isError = error != null,
            supportingText = {
                Text(error ?: "De ${SessionRules.MINUTES_MIN} a ${SessionRules.MINUTES_MAX} minutos.")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 25, 45, 60).forEach { minutes ->
                FilterChip(
                    selected = minutesText == minutes.toString(),
                    onClick = { onMinutesChange(minutes.toString()) },
                    label = { Text("$minutes min") },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
        if (validMinutes != null) {
            Text("Duração escolhida: $validMinutes minutos.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
