package com.example.gestionmovilsolicitudes.ui.admin

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.domain.ReglasAtraso
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Prioridad
import com.example.gestionmovilsolicitudes.model.etiqueta
import com.example.gestionmovilsolicitudes.ui.componentes.ContadorAnimado
import com.example.gestionmovilsolicitudes.ui.solicitud.EstadoChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
    onAbrir: (Long) -> Unit,
    onVolver: () -> Unit,
    vm: AdminPanelViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del administrador") },
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
            ) { Text("Esta sección es solo para administradores") }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------- Resumen por estado ----------
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Resumen general", style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                                Column {
                                    Text("Total", style = MaterialTheme.typography.labelLarge)
                                    ContadorAnimado(state.total, style = MaterialTheme.typography.headlineMedium)
                                }
                                Column(Modifier.clickable { vm.onSoloAtrasadas(!state.soloAtrasadas) }) {
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
                                        modifier = Modifier.clickable {
                                            vm.onEstado(if (state.filtroEstado == estado) null else estado)
                                        }
                                    ) {
                                        EstadoChip(estado)
                                        ContadorAnimado(total, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                // ---------- Por prioridad y por responsable ----------
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Por prioridad", style = MaterialTheme.typography.titleMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.porPrioridad.forEach { (nombre, total) ->
                                    AssistChip(onClick = {}, label = { Text("$nombre: $total") })
                                }
                            }
                            Text(
                                "Por responsable",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            state.porResponsable.forEach { (nombre, total) ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(nombre)
                                    ContadorAnimado(total, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }

                // ---------- Filtros ----------
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Filtros", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = state.texto,
                                onValueChange = vm::onTexto,
                                label = { Text("Buscar por código o título") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FiltroDropdown(
                                    label = "Estado",
                                    seleccion = state.filtroEstado,
                                    opciones = EstadoSolicitud.entries,
                                    texto = { it.etiqueta() },
                                    onSeleccion = vm::onEstado,
                                    modifier = Modifier.weight(1f)
                                )
                                FiltroDropdown(
                                    label = "Prioridad",
                                    seleccion = state.filtroPrioridad,
                                    opciones = Prioridad.entries,
                                    texto = { it.etiqueta() },
                                    onSeleccion = vm::onPrioridad,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FiltroDropdown(
                                    label = "Cliente",
                                    seleccion = state.clientes.firstOrNull { it.id == state.filtroClienteId },
                                    opciones = state.clientes,
                                    texto = { it.nombreCliente },
                                    onSeleccion = { vm.onCliente(it?.id) },
                                    modifier = Modifier.weight(1f)
                                )
                                FiltroDropdown(
                                    label = "Responsable",
                                    seleccion = state.tecnicos.firstOrNull { it.id == state.filtroResponsableId },
                                    opciones = state.tecnicos,
                                    texto = { it.nombre },
                                    onSeleccion = { vm.onResponsable(it?.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Switch(checked = state.soloAtrasadas, onCheckedChange = vm::onSoloAtrasadas)
                                Text("Solo atrasadas (más de ${ReglasAtraso.DIAS_PARA_ATRASO} días)")
                            }
                            if (state.hayFiltros) {
                                TextButton(onClick = vm::limpiarFiltros) { Text("Limpiar filtros") }
                            }
                        }
                    }
                }

                // ---------- Resultados ----------
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Solicitudes (${state.solicitudes.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (state.hayFiltros) {
                            TextButton(onClick = vm::limpiarFiltros) { Text("Quitar filtros") }
                        }
                    }
                }
                if (state.solicitudes.isEmpty()) {
                    item { Text("No hay solicitudes con esos filtros") }
                }
                items(state.solicitudes, key = { it.id }) { s ->
                    SolicitudAdminCard(
                        s = s,
                        onClick = { onAbrir(s.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun SolicitudAdminCard(
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

/** Menú desplegable genérico con la opción "Todos" (= sin filtro). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FiltroDropdown(
    label: String,
    seleccion: T?,
    opciones: List<T>,
    texto: (T) -> String,
    onSeleccion: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }, modifier = modifier) {
        OutlinedTextField(
            value = seleccion?.let(texto) ?: "Todos",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text("Todos") },
                onClick = { onSeleccion(null); abierto = false }
            )
            opciones.forEach { o ->
                DropdownMenuItem(
                    text = { Text(texto(o)) },
                    onClick = { onSeleccion(o); abierto = false }
                )
            }
        }
    }
}