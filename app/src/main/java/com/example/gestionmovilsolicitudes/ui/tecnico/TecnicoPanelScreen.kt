package com.example.gestionmovilsolicitudes.ui.tecnico

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionmovilsolicitudes.data.dao.ActividadReciente
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.domain.ReglasAtraso
import com.example.gestionmovilsolicitudes.model.etiqueta
import com.example.gestionmovilsolicitudes.ui.componentes.ContadorAnimado
import com.example.gestionmovilsolicitudes.ui.solicitud.EstadoChip
import com.example.gestionmovilsolicitudes.ui.solicitud.formatearFechaHora

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TecnicoPanelScreen(
    onAbrir: (Long) -> Unit,
    onVolver: () -> Unit,
    vm: TecnicoPanelViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del técnico") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            !state.autorizado -> Box(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) { Text("Esta sección es solo para técnicos") }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------- Indicadores ----------
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Mis solicitudes asignadas", style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                                Column {
                                    Text("Total", style = MaterialTheme.typography.labelLarge)
                                    ContadorAnimado(state.total, style = MaterialTheme.typography.headlineMedium)
                                }
                                Column {
                                    Text("Atrasadas", style = MaterialTheme.typography.labelLarge)
                                    ContadorAnimado(
                                        valor = state.atrasadas,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = if (state.atrasadas > 0) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Text("Por estado (toca uno para filtrar)", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.porEstado.forEach { (estado, total) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.clickable { vm.onEstado(estado) }
                                    ) {
                                        EstadoChip(estado)
                                        ContadorAnimado(total, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                // ---------- Lista de trabajo ----------
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Por atender (${state.solicitudes.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (state.filtroEstado != null) {
                            TextButton(onClick = vm::quitarFiltro) { Text("Quitar filtro") }
                        }
                    }
                }
                if (state.solicitudes.isEmpty()) {
                    item {
                        Text(
                            if (state.total == 0) "Aún no tienes solicitudes asignadas"
                            else "No hay solicitudes en ese estado"
                        )
                    }
                }
                items(state.solicitudes, key = { it.id }) { s ->
                    SolicitudTecnicoCard(
                        s = s,
                        onClick = { onAbrir(s.id) },
                        modifier = Modifier.animateItem()
                    )
                }

                // ---------- Historial de mis acciones ----------
                item { Text("Mi actividad reciente", style = MaterialTheme.typography.titleMedium) }
                if (state.actividad.isEmpty()) {
                    item { Text("Todavía no has realizado acciones") }
                }
                items(state.actividad, key = { it.id }) { a ->
                    ActividadItem(a, modifier = Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun SolicitudTecnicoCard(
    s: SolicitudEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.codigoSeguimiento, style = MaterialTheme.typography.labelLarge)
                EstadoChip(s.estado)
            }
            Text(s.titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                "${s.categoria.etiqueta()} · Prioridad: ${s.prioridad?.etiqueta() ?: "sin definir"}",
                style = MaterialTheme.typography.bodySmall
            )
            if (ReglasAtraso.esAtrasada(s)) {
                Text(
                    "Atrasada",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ActividadItem(
    a: ActividadReciente,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(formatearFechaHora(a.fechaHora), style = MaterialTheme.typography.labelSmall)
            Text("${a.codigo} · ${a.accion}", style = MaterialTheme.typography.titleSmall)
            Text(a.titulo, style = MaterialTheme.typography.bodySmall)
            a.comentario?.let {
                Text("\"$it\"", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
            }
        }
    }
}