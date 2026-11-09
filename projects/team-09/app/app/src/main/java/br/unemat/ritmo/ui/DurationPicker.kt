package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun DurationPicker(selectedMinutes: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quanto tempo você tem?", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(15, 25, 45).forEach { minutes ->
                FilterChip(
                    selected = selectedMinutes == minutes,
                    onClick = { onSelect(minutes) },
                    label = { Text("$minutes min") },
                    modifier = Modifier.heightIn(min = 48.dp).semantics { role = Role.RadioButton }
                )
            }
        }
        Text("Duração escolhida: $selectedMinutes minutos.", color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}
