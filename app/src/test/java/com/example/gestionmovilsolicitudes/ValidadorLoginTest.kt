package com.example.gestionmovilsolicitudes

import com.example.gestionmovilsolicitudes.domain.ValidadorLogin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorLoginTest {

    @Test
    fun camposVaciosGeneranUnErrorPorCampo() {
        val r = ValidadorLogin.validar("", "")

        assertEquals("Ingresa tu correo", r.correo)
        assertEquals("Ingresa tu contraseña", r.password)
        assertTrue(!r.valido)
    }
}
