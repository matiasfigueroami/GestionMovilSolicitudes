package com.example.gestionmovilsolicitudes.ui.indicadores

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionmovilsolicitudes.data.repository.Indicador

/** Tarjeta del menú con los indicadores económicos del día (API externa mindicador.cl vía Retrofit). */
@Composable
fun IndicadoresCard(
    modifier: Modifier = Modifier,
    vm: IndicadoresViewModel = viewModel()
) {
    val st by vm.uiState.collectAsState()

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Indicadores económicos", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Referencia para cotizar servicios · fuente: mindicador.cl",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = vm::cargar, enabled = !st.cargando) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar indicadores")
                }
            }
            HorizontalDivider()

            AnimatedContent(targetState = st, label = "indicadores") { estado ->
                when {
                    estado.cargando && estado.indicadores.isEmpty() -> Box(
                        Modifier.fillMaxWidth().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(Modifier.size(28.dp)) }

                    estado.error != null && estado.indicadores.isEmpty() -> Column {
                        Text(
                            estado.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = vm::cargar) { Text("Reintentar") }
                    }

                    else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        estado.indicadores.forEach { FilaIndicador(it) }
                        estado.indicadores.firstOrNull()?.fecha?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                "Valores al $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaIndicador(i: Indicador) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(i.nombre, style = MaterialTheme.typography.bodyLarge)
        Text(i.valor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
