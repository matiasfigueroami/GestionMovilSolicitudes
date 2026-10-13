package com.example.gestionmovilsolicitudes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionmovilsolicitudes.session.SessionManager

@Composable
fun HomeTemporal(titulo: String) {
    val usuario by SessionManager.usuario.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineMedium)
        Text("Hola, ${usuario?.nombre ?: ""}")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { SessionManager.cerrar() }) { Text("Cerrar sesión") }
    }
}