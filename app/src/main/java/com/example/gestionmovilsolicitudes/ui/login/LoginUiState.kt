package com.example.gestionmovilsolicitudes.ui.login

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,

    val correoError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,       // error general (credenciales incorrectas)

    val cargando: Boolean = false
)