package com.example.gestionmovilsolicitudes.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionmovilsolicitudes.GestionApp
import com.example.gestionmovilsolicitudes.domain.ValidadorLogin
import com.example.gestionmovilsolicitudes.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val gestion = app as GestionApp

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(v: String) =
        _uiState.update { it.copy(correo = v, correoError = null, error = null) }

    fun onPasswordChange(v: String) =
        _uiState.update { it.copy(password = v, passwordError = null, error = null) }

    fun alternarPassword() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun entrar() {
        val st = _uiState.value
        if (st.cargando) return

        // 1. Validar por campo: los errores se publican en el estado
        val r = ValidadorLogin.validar(st.correo, st.password)
        if (!r.valido) {
            _uiState.update { it.copy(correoError = r.correo, passwordError = r.password, error = null) }
            return
        }

        // 2. Autenticar contra el microservicio de usuarios (POST /api/auth/login)
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val usuario = gestion.repository.login(st.correo.trim(), st.password)
                SessionManager.iniciar(usuario)
                _uiState.update { it.copy(cargando = false) }
            } catch (e: Exception) {
                // Credenciales incorrectas o servidor apagado: el mensaje viene listo para mostrar
                _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo iniciar sesión") }
            }
        }
    }
}