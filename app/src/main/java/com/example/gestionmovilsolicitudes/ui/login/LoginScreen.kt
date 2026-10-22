package com.example.gestionmovilsolicitudes.ui.login

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionmovilsolicitudes.ui.componentes.CampoFormulario
import com.example.gestionmovilsolicitudes.ui.componentes.ErrorAnimado

@Composable
fun LoginScreen(vm: LoginViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de solicitudes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        CampoFormulario(
            value = state.correo,
            onValueChange = vm::onCorreoChange,
            label = "Correo",
            icono = Icons.Default.Email,
            error = state.correoError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(4.dp))
        CampoFormulario(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            label = "Contraseña",
            icono = Icons.Default.Lock,
            error = state.passwordError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { vm.entrar() }),
            visualTransformation = if (state.passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailing = {
                TextButton(onClick = vm::alternarPassword) {
                    Text(if (state.passwordVisible) "Ocultar" else "Mostrar")
                }
            }
        )

        ErrorAnimado(state.error, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { vm.entrar() },
            enabled = !state.cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            AnimatedVisibility(
                visible = state.cargando,
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
            Text(if (state.cargando) "Ingresando..." else "Ingresar")
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Demo: admin@demo.cl · tecnico1@demo.cl · cliente1@demo.cl\nContraseña: 1234",
            style = MaterialTheme.typography.bodySmall
        )
    }
}