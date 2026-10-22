package com.example.gestionmovilsolicitudes.ui.componentes

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/** Número que "cuenta" hasta su valor al aparecer y cada vez que cambia. */
@Composable
fun ContadorAnimado(
    valor: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    var objetivo by remember { mutableIntStateOf(0) }
    LaunchedEffect(valor) { objetivo = valor }
    val animado by animateIntAsState(targetValue = objetivo, animationSpec = tween(700), label = "contador")
    Text("$animado", modifier = modifier, style = style, color = color)
}