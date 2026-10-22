package com.example.gestionmovilsolicitudes.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Mensaje de error que aparece y desaparece con una animación suave. */
@Composable
fun ErrorAnimado(
    mensaje: String?,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    // Se recuerda el último texto para que no quede vacío mientras se anima la salida
    var ultimo by remember { mutableStateOf("") }
    if (mensaje != null) ultimo = mensaje

    AnimatedVisibility(
        visible = mensaje != null,
        modifier = modifier,
        enter = fadeIn(tween(200)) + expandVertically(tween(200)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
    ) {
        Text(ultimo, color = color)
    }
}