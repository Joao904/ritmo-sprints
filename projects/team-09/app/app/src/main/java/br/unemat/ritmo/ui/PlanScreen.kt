package br.unemat.ritmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PlanScreen(selectedMinutes: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(containerColor = Paper) { padding ->
        Column(
            modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("Voltar") }
                Spacer(Modifier.weight(1f))
                Text("RITMO / SEU ESTUDO", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Seu plano", style = MaterialTheme.typography.headlineLarge)
                Text("Um tempo reservado para aprender.", color = Muted, style = MaterialTheme.typography.bodyLarge)
            }
            Surface(color = Forest, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TEMPO DE CONCENTRAÇÃO", color = Lime, style = MaterialTheme.typography.labelMedium)
                    Text("$selectedMinutes minutos", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Text("Um assunto por vez é suficiente.", color = Color(0xFFD6E3DD))
                }
            }
            Text("Um roteiro simples", style = MaterialTheme.typography.titleLarge)
            PlanStep("01", "Prepare seu espaço", "Separe o material e escolha um único assunto para estudar.")
            PlanStep("02", "Concentre-se", "Dedique os $selectedMinutes minutos escolhidos ao assunto, respeitando seu ritmo.")
            PlanStep("03", "Faça uma pausa", "Ao terminar, levante-se, respire e reconheça o que aprendeu.")
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Ajustar duração", style = MaterialTheme.typography.titleMedium)
            }
            Text("Este é seu plano de estudo, sem contagem de tempo.", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PlanStep(number: String, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = Lime, shape = RoundedCornerShape(12.dp)) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Text(number, style = MaterialTheme.typography.labelLarge, color = Forest)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanPreview() { RitmoTheme { PlanScreen(25, {}) } }
