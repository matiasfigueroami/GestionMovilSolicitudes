package com.example.gestionmovilsolicitudes.ui.solicitud

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun crearArchivoEvidencia(context: Context): File {
    val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
    return File(carpeta, "EVID_${System.currentTimeMillis()}.jpg")
}

fun uriParaCamara(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
