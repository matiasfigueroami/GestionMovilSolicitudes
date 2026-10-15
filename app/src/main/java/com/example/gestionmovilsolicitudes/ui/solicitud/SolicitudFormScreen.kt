package com.example.gestionmovilsolicitudes.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.etiqueta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    solicitudId: Long?,
    onVolver: () -> Unit,
    vm: SolicitudViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    var menuAbierto by remember { mutableStateOf(false) }

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
                supportingText = { Text(state.tituloError ?: "") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.descripcion,
                onValueChange = vm::onDescripcionChange,
                label = { Text("Descripción") },
                minLines = 3,
                isError = state.descripcionError != null,
                supportingText = { Text(state.descripcionError ?: "") },
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
                    supportingText = { Text(state.categoriaError ?: "") },
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
                supportingText = { Text(state.servicioError ?: "") },
                modifier = Modifier.fillMaxWidth()
            )

            state.mensajeError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = vm::guardar,
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (state.guardando) "Guardando..." else "Guardar")
            }
        }
    }
}