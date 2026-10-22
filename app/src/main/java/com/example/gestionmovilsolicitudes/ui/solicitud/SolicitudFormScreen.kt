package com.example.gestionmovilsolicitudes.ui.solicitud

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.etiqueta
import com.example.gestionmovilsolicitudes.ui.componentes.ErrorAnimado
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    solicitudId: Long?,
    onVolver: () -> Unit,
    vm: SolicitudViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    var menuAbierto by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val archivo = rutaPendiente?.let { File(it) }
        if (exito && archivo != null) {
            vm.onEvidenciaChange(Uri.fromFile(archivo).toString())
        } else {
            archivo?.delete()
        }
        rutaPendiente = null
    }
    val tomarFoto = {
        val archivo = crearArchivoEvidencia(context)
        rutaPendiente = archivo.absolutePath
        camara.launch(uriParaCamara(context, archivo))
    }

    // Al entrar: formulario vacío (nueva) o datos cargados (editar)
    LaunchedEffect(solicitudId) {
        if (solicitudId == null) vm.limpiarFormulario() else vm.cargarParaEditar(solicitudId)
    }
    // Al guardar bien: volver a la lista
    LaunchedEffect(state.guardadoExitosamente) {
        if (state.guardadoExitosamente) {
            vm.limpiarFormulario()
            onVolver()
        }
    }

    val categoriaSeleccionada = Categoria.entries.firstOrNull { it.name == state.categoria }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.editandoId == null) "Nueva solicitud" else "Solicitud ${state.codigo.orEmpty()}")
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.titulo,
                onValueChange = vm::onTituloChange,
                label = { Text("Título") },
                singleLine = true,
                isError = state.tituloError != null,
                supportingText = { ErrorAnimado(state.tituloError) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.descripcion,
                onValueChange = vm::onDescripcionChange,
                label = { Text("Descripción") },
                minLines = 3,
                isError = state.descripcionError != null,
                supportingText = { ErrorAnimado(state.descripcionError) },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = menuAbierto, onExpandedChange = { menuAbierto = it }) {
                OutlinedTextField(
                    value = categoriaSeleccionada?.etiqueta().orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                    isError = state.categoriaError != null,
                    supportingText = { ErrorAnimado(state.categoriaError) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    Categoria.entries.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.etiqueta()) },
                            onClick = {
                                vm.onCategoriaChange(c.name)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.servicio,
                onValueChange = vm::onServicioChange,
                label = { Text("Servicio relacionado") },
                singleLine = true,
                isError = state.servicioError != null,
                supportingText = { ErrorAnimado(state.servicioError) },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Evidencia (foto)", style = MaterialTheme.typography.titleSmall)
            state.evidenciaUri?.let { uri ->
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { tomarFoto() }) {
                    Text(if (state.evidenciaUri == null) "Tomar foto" else "Cambiar foto")
                }
                if (state.evidenciaUri != null) {
                    TextButton(onClick = { vm.onEvidenciaChange(null) }) { Text("Quitar") }
                }
            }

            state.mensajeError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = vm::guardar,
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                // Indicador de progreso que aparece mientras se guarda
                AnimatedVisibility(
                    visible = state.guardando,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Text(if (state.guardando) "Guardando..." else "Guardar")
            }
        }
    }
}