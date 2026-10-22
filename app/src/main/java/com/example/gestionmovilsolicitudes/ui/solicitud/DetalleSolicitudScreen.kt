package com.example.gestionmovilsolicitudes.ui.solicitud

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gestionmovilsolicitudes.data.dao.EventoHistorial
import com.example.gestionmovilsolicitudes.domain.MensajeSeguimiento
import com.example.gestionmovilsolicitudes.domain.ValidadorTexto
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad
import com.example.gestionmovilsolicitudes.model.etiqueta
import com.example.gestionmovilsolicitudes.ui.componentes.CampoFormulario

private enum class Dialogo { REABRIR, RESPONDER, CANCELAR, AVANCE, PEDIR_INFO, SOLUCION }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetalleSolicitudScreen(
    solicitudId: Long,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    vm: DetalleViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var dialogo by remember { mutableStateOf<Dialogo?>(null) }
    val context = LocalContext.current
    LaunchedEffect(solicitudId) { vm.cargar(solicitudId) }

    // Muestra cada mensaje del ViewModel una sola vez
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.consumirMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.solicitud?.codigoSeguimiento ?: "Solicitud") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (state.puedeEditar) {
                        IconButton(onClick = onEditar) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar solicitud")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val s = state.solicitud
        when {
            state.cargando -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            s == null -> Box(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró la solicitud o no tienes acceso a ella")
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---- Encabezado: título, estado vigente y prioridad ----
                item {
                    Card(Modifier.fillMaxWidth().animateContentSize()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    s.titulo,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                EstadoChip(s.estado)
                            }
                            Text(
                                "Prioridad: ${s.prioridad?.etiqueta() ?: "sin definir"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Última actualización: ${formatearFechaHora(s.fechaActualizacion)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedButton(
                                onClick = {
                                    compartirTexto(
                                        context,
                                        "Solicitud ${s.codigoSeguimiento}",
                                        MensajeSeguimiento.construir(s)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Compartir referencia") }
                        }
                    }
                }

                // ---- Datos de la solicitud ----
                item {
                    Card(Modifier.fillMaxWidth().animateContentSize()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Datos", style = MaterialTheme.typography.titleMedium)
                            Fila("Categoría", s.categoria.etiqueta())
                            Fila("Servicio", s.servicioRelacionado ?: "—")
                            s.sitioOActivoRelacionado?.let { Fila("Sitio o activo", it) }
                            if (state.perfil != Perfil.CLIENTE) Fila(
                                "Cliente",
                                state.clienteNombre ?: "—"
                            )
                            Fila("Responsable", state.responsableNombre ?: "Sin asignar")
                            Fila("Creada", formatearFechaHora(s.fechaCreacion))
                            s.fechaResolucion?.let { Fila("Resuelta", formatearFechaHora(it)) }
                        }
                    }
                }

                // ---- Descripción ----
                item {
                    Card(Modifier.fillMaxWidth().animateContentSize()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Descripción", style = MaterialTheme.typography.titleMedium)
                            Text(s.descripcion, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // ---- Evidencia fotográfica (si la solicitud tiene una) ----
                s.evidenciaUri?.let { uri ->
                    item {
                        Card(Modifier.fillMaxWidth().animateContentSize()) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Evidencia", style = MaterialTheme.typography.titleMedium)
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Foto de evidencia de la solicitud",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                )
                            }
                        }
                    }
                }

                // ---- Gestión (solo administrador) ----
                if (state.esAdmin) {
                    item {
                        Card(Modifier.fillMaxWidth().animateContentSize()) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Gestión", style = MaterialTheme.typography.titleMedium)

                                // ---- Prioridad ----
                                Text("Prioridad", style = MaterialTheme.typography.labelLarge)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Prioridad.values().forEach { p ->
                                        FilterChip(
                                            selected = s.prioridad == p,
                                            onClick = { vm.cambiarPrioridad(p) },
                                            label = { Text(p.etiqueta()) },
                                            enabled = !state.procesando
                                        )
                                    }
                                }

                                // ---- Responsable ----
                                Text("Responsable", style = MaterialTheme.typography.labelLarge)
                                var expandido by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(
                                        onClick = { expandido = true },
                                        enabled = !state.procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(state.responsableNombre ?: "Asignar técnico")
                                    }
                                    DropdownMenu(
                                        expanded = expandido,
                                        onDismissRequest = { expandido = false }
                                    ) {
                                        state.tecnicos.forEach { t ->
                                            DropdownMenuItem(
                                                text = { Text(t.nombre) },
                                                onClick = {
                                                    vm.asignarTecnico(t.id)
                                                    expandido = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // ---- Cambiar estado ----
                                if (state.estadosDisponibles.isNotEmpty()) {
                                    Text(
                                        "Cambiar estado",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        state.estadosDisponibles.forEach { estado ->
                                            if (estado == EstadoSolicitud.CANCELADA) {
                                                OutlinedButton(
                                                    onClick = { dialogo = Dialogo.CANCELAR },
                                                    enabled = !state.procesando
                                                ) { Text(estado.etiqueta()) }
                                            } else {
                                                OutlinedButton(
                                                    onClick = { vm.cambiarEstadoAdmin(estado) },
                                                    enabled = !state.procesando
                                                ) { Text(estado.etiqueta()) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ---- Trabajo del técnico ----
                if (state.tieneAccionesTecnico) {
                    item {
                        Card(Modifier.fillMaxWidth().animateContentSize()) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Trabajo técnico", style = MaterialTheme.typography.titleMedium)
                                if (state.puedeIniciar) {
                                    Button(
                                        onClick = vm::iniciarTrabajo,
                                        enabled = !state.procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Iniciar trabajo") }
                                }
                                if (state.puedeRegistrarAvance) {
                                    OutlinedButton(
                                        onClick = { dialogo = Dialogo.AVANCE },
                                        enabled = !state.procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Registrar avance") }
                                }
                                if (state.puedePedirInformacion) {
                                    OutlinedButton(
                                        onClick = { dialogo = Dialogo.PEDIR_INFO },
                                        enabled = !state.procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Pedir información al cliente") }
                                }
                                if (state.puedeResolver) {
                                    Button(
                                        onClick = { dialogo = Dialogo.SOLUCION },
                                        enabled = !state.procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Registrar solución y resolver") }
                                }
                            }
                        }
                    }
                }

                // ---- Acciones del cliente según el estado ----
                if (state.puedeResponder || state.puedeConfirmar || state.puedeReabrir) {
                    item {
                        Card(Modifier.fillMaxWidth().animateContentSize()) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Acciones", style = MaterialTheme.typography.titleMedium)
                                if (state.puedeResponder) {
                                    Text("La empresa necesita más información para continuar.")
                                    state.ultimaConsulta?.let { Cita("Consulta de la empresa", it) }
                                    Button(
                                        onClick = { dialogo = Dialogo.RESPONDER },
                                        enabled = !state.procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Responder solicitud de información") }
                                }
                                if (state.puedeConfirmar) {
                                    Text("La empresa registró una solución. Confirms si estás conforme.")
                                    state.ultimaSolucion?.let { Cita("Solución propuesta", it) }
                                    Button(
                                        onClick = vm::confirmarResolucion,
                                        enabled = !state.procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Confirmar resolución") }
                                }
                                if (state.puedeReabrir) {
                                    OutlinedButton(
                                        onClick = { dialogo = Dialogo.REABRIR },
                                        enabled = !state.procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Solicitar reapertura") }
                                }
                            }
                        }
                    }
                }

                // ---- Agregar comentario ----
                if (state.puedeComentar) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoFormulario(
                                value = state.comentario,
                                onValueChange = vm::onComentarioChange,
                                label = "Agregar comentario",
                                icono = Icons.Default.Create,
                                error = state.comentarioError,
                                singleLine = false,
                                minLines = 2
                            )
                            Button(
                                onClick = vm::enviarComentario,
                                enabled = !state.procesando,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Enviar comentario") }
                        }
                    }
                }

                // ---- Historial cronológico ----
                item { Text("Historial", style = MaterialTheme.typography.titleMedium) }
                items(state.historial, key = { it.id }) { evento ->
                    HistorialItem(evento, modifier = Modifier.animateItem())
                }
            }
        }
    }

    dialogo?.let { d ->
        when (d) {
            Dialogo.REABRIR -> DialogoTexto(
                titulo = "Solicitar reapertura",
                etiqueta = "Motivo de la reapertura",
                onConfirm = { vm.reabrir(it); dialogo = null },
                onDismiss = { dialogo = null }
            )

            Dialogo.RESPONDER -> DialogoTexto(
                titulo = "Responder a la empresa",
                etiqueta = "Tu respuesta",
                onConfirm = { vm.responderInformacion(it); dialogo = null },
                onDismiss = { dialogo = null }
            )

            Dialogo.CANCELAR -> DialogoTexto(
                titulo = "Cancelar solicitud",
                etiqueta = "Motivo de la cancelación",
                onConfirm = { vm.cancelarSolicitud(it); dialogo = null },
                onDismiss = { dialogo = null }
            )

            Dialogo.AVANCE -> DialogoTexto(
                titulo = "Registrar avance",
                etiqueta = "¿Qué avanzaste?",
                onConfirm = { vm.registrarAvance(it); dialogo = null },
                onDismiss = { dialogo = null }
            )

            Dialogo.PEDIR_INFO -> DialogoTexto(
                titulo = "Pedir información al cliente",
                etiqueta = "¿Qué necesitas saber?",
                onConfirm = { vm.solicitarInformacion(it); dialogo = null },
                onDismiss = { dialogo = null }
            )

            Dialogo.SOLUCION -> DialogoTexto(
                titulo = "Registrar solución",
                etiqueta = "Describe la solución aplicada",
                onConfirm = { vm.registrarSolucion(it); dialogo = null },
                onDismiss = { dialogo = null }
            )
        }
    }
}

@Composable
private fun Cita(titulo: String, texto: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelLarge)
        Text("\"$texto\"", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(valor, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun HistorialItem(e: EventoHistorial, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(formatearFechaHora(e.fechaHora), style = MaterialTheme.typography.labelSmall)
            Text(e.accionRealizada, style = MaterialTheme.typography.titleSmall)
            if (e.estadoAnterior != null && e.estadoAnterior != e.estadoNuevo) {
                Text(
                    "Estado: ${e.estadoAnterior.etiqueta()} → ${e.estadoNuevo.etiqueta()}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (e.estadoAnterior == null) {
                Text("Estado inicial: ${e.estadoNuevo.etiqueta()}", style = MaterialTheme.typography.bodySmall)
            }
            e.comentario?.let {
                Text("\"$it\"", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
            }
            Text(
                "Por ${e.nombreUsuario} (${e.perfil.etiqueta()})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DialogoTexto(
    titulo: String,
    etiqueta: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            CampoFormulario(
                value = texto,
                onValueChange = {
                    texto = it
                    error = null
                },
                label = etiqueta,
                icono = Icons.Default.Create,
                error = error,
                singleLine = false,
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(onClick = {
                // La regla de validación vive en el dominio; el diálogo solo muestra el error
                val problema = ValidadorTexto.validar(texto)
                if (problema != null) error = problema else onConfirm(texto)
            }) { Text("Enviar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}