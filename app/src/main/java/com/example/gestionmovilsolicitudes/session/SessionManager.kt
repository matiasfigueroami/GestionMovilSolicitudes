package com.example.gestionmovilsolicitudes.session

import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario.asStateFlow()

    fun iniciar(usuario: UsuarioEntity) { _usuario.value = usuario }
    fun cerrar() { _usuario.value = null }
}