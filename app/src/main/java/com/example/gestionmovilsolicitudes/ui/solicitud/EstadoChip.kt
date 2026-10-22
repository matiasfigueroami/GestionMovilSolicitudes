package com.example.gestionmovilsolicitudes.ui.solicitud

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.etiqueta
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
private fun colorEstado(estado: EstadoSolicitud): Color = when (estado) {
    EstadoSolicitud.RECIBIDA -> Color(0xFF1565C0)
    EstadoSolicitud.EN_REVISION -> Color(0xFF6A1B9A)
    EstadoSolicitud.PENDIENTE_INFORMACION -> Color(0xFFBF360C)
    EstadoSolicitud.ASIGNADA -> Color(0xFF00695C)
    EstadoSolicitud.EN_PROCESO -> Color(0xFF283593)
    EstadoSolicitud.RESUELTA -> Color(0xFF2E7D32)
    EstadoSolicitud.CERRADA -> Color(0xFF424242)
    EstadoSolicitud.CANCELADA -> Color(0xFFB71C1C)
}

@Composable
fun EstadoChip(estado: EstadoSolicitud, modifier: Modifier = Modifier) {
    // El color y el texto cambian con una transición cuando cambia el estado
    val color by animateColorAsState(colorEstado(estado), tween(400), label = "colorEstado")
    Surface(color = color, shape = MaterialTheme.shapes.small, modifier = modifier) {
        AnimatedContent(
            targetState = estado,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
            label = "textoEstado"
        ) { e ->
            Text(
                text = e.etiqueta(),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}