package com.example.gestionmovilsolicitudes.ui.solicitud

import android.content.Context
import android.content.Intent

/** Abre el menú "Compartir" de Android con el texto indicado (WhatsApp, correo, etc.). */
fun compartirTexto(context: Context, asunto: String, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir referencia"))
}