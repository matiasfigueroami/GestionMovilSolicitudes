package com.example.gestionmovilsolicitudes.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.etiqueta
import com.example.gestionmovilsolicitudes.session.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(
    onNueva: () -> Unit,
    onAbrir: (Long) -> Unit,
    onVolver: () -> Unit,
    vm: SolicitudViewModel = viewModel()
) {
    val solicitudes by vm.solicitudes.collectAsState()
    val usuario by SessionManager.usuario.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (usuario?.perfil == Perfil.CLIENTE) {
                FloatingActionButton(onClick = onNueva) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva solicitud")
                }
            }
        }
    ) { padding ->
        if (solicitudes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aún no hay solicitudes", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(solicitudes, key = { it.id }) { s ->
                    SolicitudCard(
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
private fun SolicitudCard(
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
            Text(s.categoria.etiqueta(), style = MaterialTheme.typography.bodySmall)
        }
    }
}