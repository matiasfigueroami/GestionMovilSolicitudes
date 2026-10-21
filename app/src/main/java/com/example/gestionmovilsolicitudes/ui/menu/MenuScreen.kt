package com.example.gestionmovilsolicitudes.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.session.SessionManager

@Composable
fun MenuScreen(
    onSolicitudes: () -> Unit,
    onPanelAdmin: () -> Unit,
    onPanelTecnico: () -> Unit,
    onCerrarSesion: () -> Unit
) {    val usuario by SessionManager.usuario.collectAsState()
    val perfil = usuario?.perfil?.name?.lowercase()?.replaceFirstChar { it.uppercase() }.orEmpty()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Menú principal", style = MaterialTheme.typography.headlineMedium)

        if (usuario?.perfil == Perfil.ADMINISTRADOR) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = onPanelAdmin, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Panel del administrador")
            }
        }
        if (usuario?.perfil == Perfil.TECNICO) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = onPanelTecnico, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Panel del técnico")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.titleMedium)
        Text("Perfil: $perfil", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        Button(onClick = onSolicitudes, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Solicitudes")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Cerrar sesión")
        }
    }
}

