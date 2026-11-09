package br.unemat.ritmo.ui

import androidx.compose.foundation.background
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
fun WelcomeScreen(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
    onPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(containerColor = Paper) { padding ->
        Column(
            modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Brand()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("MENOS PRESSA. MAIS PRESENÇA.", style = MaterialTheme.typography.labelSmall, color = Muted)
                Text("Estude no\nseu ritmo.", style = MaterialTheme.typography.headlineLarge)
                Text("Uma sessão de cada vez.\nTransforme alguns minutos livres em um plano de estudo possível.",
                    style = MaterialTheme.typography.bodyLarge, color = Muted)
            }
            FocusCard()
            DurationPicker(selectedMinutes = selectedMinutes, onSelect = onSelect)
            Button(onClick = onPlan, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Planejar meu estudo", style = MaterialTheme.typography.titleMedium)
            }
            Text("Seu tempo. Seu próximo passo.", style = MaterialTheme.typography.labelMedium, color = Muted)
        }
    }
}

@Composable
fun Brand() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(color = Forest, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.size(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                listOf(12, 22, 12).forEach { height ->
                    Box(Modifier.padding(horizontal = 2.dp).width(3.dp).height(height.dp).background(Lime, RoundedCornerShape(2.dp)))
                }
            }
        }
        Text("Ritmo", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.weight(1f))
        Text("ESTUDO & FOCO", style = MaterialTheme.typography.labelSmall, color = Muted)
    }
}

@Composable
private fun FocusCard() {
    Surface(color = Forest, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("UM PASSO DE CADA VEZ", color = Lime, style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("01", color = Lime, style = MaterialTheme.typography.headlineLarge)
                Text("Um assunto.\nToda a sua atenção.", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            Text("Começar pequeno também é avançar.", color = Color(0xFFD6E3DD), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePreview() { RitmoTheme { WelcomeScreen(25, {}, {}) } }
