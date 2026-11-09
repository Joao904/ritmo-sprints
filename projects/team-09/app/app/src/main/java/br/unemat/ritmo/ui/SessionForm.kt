package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import br.unemat.ritmo.model.SessionRules

/**
 * Campos controlados: o texto vem de [subject]/[goal] (estado no RitmoApp)
 * e cada digitação volta por [onSubjectChange]/[onGoalChange].
 * As mensagens de erro só aparecem depois da primeira tentativa ([showErrors]).
 */
@Composable
fun SessionForm(
    subject: String,
    goal: String,
    showErrors: Boolean,
    onSubjectChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
) {
    val subjectError = if (showErrors) SessionRules.subjectError(subject) else null
    val goalError = if (showErrors) SessionRules.goalError(goal) else null

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("O que você vai estudar?", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = subject,
            onValueChange = onSubjectChange,
            label = { Text("Assunto") },
            placeholder = { Text("Ex.: Cálculo I — limites") },
            singleLine = true,
            isError = subjectError != null,
            supportingText = {
                FieldHelp(
                    message = subjectError ?: "Obrigatório.",
                    counter = "${subject.trim().length}/${SessionRules.SUBJECT_MAX}"
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = goal,
            onValueChange = onGoalChange,
            label = { Text("Objetivo (opcional)") },
            placeholder = { Text("Ex.: resolver 5 exercícios") },
            isError = goalError != null,
            supportingText = {
                FieldHelp(
                    message = goalError ?: "Opcional.",
                    counter = "${goal.trim().length}/${SessionRules.GOAL_MAX}"
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FieldHelp(message: String, counter: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(message, modifier = Modifier.weight(1f))
        Text(counter)
    }
}
