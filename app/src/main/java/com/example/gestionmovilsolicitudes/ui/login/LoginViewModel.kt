package com.example.gestionmovilsolicitudes.ui.login

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionmovilsolicitudes.GestionApp
import com.example.gestionmovilsolicitudes.session.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val gestion = app as GestionApp

    var correo by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var cargando by mutableStateOf(false)

    fun entrar() {
        if (correo.isBlank() || password.isBlank()) {
            error = "Ingresa tu correo y contraseña"
            return
        }
        viewModelScope.launch {
            cargando = true
            error = null
            gestion.seedJob.join() // espera a que existan los datos de prueba
            val usuario = gestion.database.usuarioDao().login(correo.trim(), password)
            if (usuario == null) {
                error = "Correo o contraseña incorrectos"
            } else {
                SessionManager.iniciar(usuario)
            }
            cargando = false
        }
    }
}