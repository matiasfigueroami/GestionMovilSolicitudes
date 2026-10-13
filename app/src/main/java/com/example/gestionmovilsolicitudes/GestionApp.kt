package com.example.gestionmovilsolicitudes

import android.app.Application
import com.example.gestionmovilsolicitudes.data.AppDatabase
import com.example.gestionmovilsolicitudes.data.SeedData
import com.example.gestionmovilsolicitudes.data.repository.SolicitudRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GestionApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { SolicitudRepository(database) }

    private val scope = CoroutineScope(Dispatchers.IO)

    // Se guarda el Job para poder esperar a que termine el sembrado antes del login
    val seedJob by lazy { scope.launch { SeedData.sembrarSiVacia(database) } }

    override fun onCreate() {
        super.onCreate()
        seedJob // fuerza que el sembrado parta al abrir la app
    }
}