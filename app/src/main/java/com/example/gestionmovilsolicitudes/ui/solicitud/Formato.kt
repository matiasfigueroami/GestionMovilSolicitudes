package com.example.gestionmovilsolicitudes.ui.solicitud

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(millis))