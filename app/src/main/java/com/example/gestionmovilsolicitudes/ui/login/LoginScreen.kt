package com.example.gestionmovilsolicitudes.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionmovilsolicitudes.ui.componentes.ErrorAnimado

@Composable
fun LoginScreen(vm: LoginViewModel = viewModel()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de solicitudes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = vm.correo,
            onValueChange = { vm.correo = it },
            label = { Text("Correo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = vm.password,
            onValueChange = { vm.password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        ErrorAnimado(vm.error, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { vm.entrar() },
            enabled = !vm.cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            AnimatedVisibility(
                visible = vm.cargando,
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
            Text(if (vm.cargando) "Ingresando..." else "Ingresar")
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Demo: admin@demo.cl · tecnico1@demo.cl · cliente1@demo.cl\nContraseña: 1234",
            style = MaterialTheme.typography.bodySmall
        )
    }
}