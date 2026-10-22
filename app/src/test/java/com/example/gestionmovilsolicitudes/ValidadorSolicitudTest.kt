package com.example.gestionmovilsolicitudes

import com.example.gestionmovilsolicitudes.domain.ValidadorSolicitud
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorSolicitudTest {

    @Test
    fun formularioVacioMarcaTodosLosCamposObligatorios() {
        val r = ValidadorSolicitud.validar("", "", "", "")

        assertEquals("El título es obligatorio", r.titulo)
        assertEquals("La descripción es obligatoria", r.descripcion)
        assertEquals("Selecciona una categoría", r.categoria)
        assertTrue(!r.valido)
    }

    @Test
    fun descripcionCortaEsRechazada() {
        val r = ValidadorSolicitud.validar("Título", "corta", "CONSULTA", "")

        assertNotNull(r.descripcion)
    }

    @Test
    fun tituloMuyLargoEsRechazado() {
        val r = ValidadorSolicitud.validar("a".repeat(ValidadorSolicitud.TITULO_MAX + 1), "Descripción válida", "CONSULTA", "")

        assertNotNull(r.titulo)
    }

    @Test
    fun servicioEsObligatorioSoloEnCategoriasQueLoRequieren() {
        val hosting = ValidadorSolicitud.validar("Título", "Descripción válida", "HOSTING", "")
        val consulta = ValidadorSolicitud.validar("Título", "Descripción válida", "CONSULTA", "")

        assertEquals("Indica el servicio relacionado", hosting.servicio)
        assertNull(consulta.servicio)
    }

    @Test
    fun sitioEsOpcionalPeroTieneLargoMaximo() {
        val sinSitio = ValidadorSolicitud.validar("Título", "Descripción válida", "CONSULTA", "")
        val sitioLargo = ValidadorSolicitud.validar(
            "Título", "Descripción válida", "CONSULTA", "", "x".repeat(ValidadorSolicitud.SITIO_MAX + 1)
        )

        assertNull(sinSitio.sitio)
        assertNotNull(sitioLargo.sitio)
    }

    @Test
    fun formularioCorrectoEsValido() {
        val r = ValidadorSolicitud.validar("Sitio caído", "El sitio no carga desde la mañana", "HOSTING", "Hosting básico")

        assertTrue(r.valido)
    }
}
