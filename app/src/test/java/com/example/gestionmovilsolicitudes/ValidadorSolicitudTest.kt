package com.example.gestionmovilsolicitudes

import com.example.gestionmovilsolicitudes.domain.ValidadorSolicitud
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorSolicitudTest {

    @Test
    fun formularioVacioMarcaLosCamposObligatorios() {
        val r = ValidadorSolicitud.validar("", "", "", "")

        assertEquals("El título es obligatorio", r.titulo)
        assertEquals("La descripción es obligatoria", r.descripcion)
        assertEquals("Selecciona una categoría", r.categoria)
        assertTrue(!r.valido)
    }
}
