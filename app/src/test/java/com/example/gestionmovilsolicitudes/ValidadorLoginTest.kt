package com.example.gestionmovilsolicitudes

import com.example.gestionmovilsolicitudes.domain.ValidadorLogin
import com.example.gestionmovilsolicitudes.domain.ValidadorTexto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun correoSinFormatoValidoEsRechazado() {
        assertNotNull(ValidadorLogin.validar("correo-sin-arroba", "1234").correo)
        assertNotNull(ValidadorLogin.validar("a@b", "1234").correo)
    }

    @Test
    fun contrasenaCortaEsRechazada() {
        assertNotNull(ValidadorLogin.validar("admin@demo.cl", "123").password)
    }

    @Test
    fun credencialesConFormatoCorrectoSonValidas() {
        assertTrue(ValidadorLogin.validar("admin@demo.cl", "1234").valido)
    }

    @Test
    fun textoLibreVacioOMuyCortoEsRechazado() {
        assertNotNull(ValidadorTexto.validar(""))
        assertNotNull(ValidadorTexto.validar("  "))
        assertNotNull(ValidadorTexto.validar("ab"))
        assertNull(ValidadorTexto.validar("Sí, dos veces"))
    }
}
