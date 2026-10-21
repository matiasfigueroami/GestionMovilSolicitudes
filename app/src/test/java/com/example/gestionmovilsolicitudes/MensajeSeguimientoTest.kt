package com.example.gestionmovilsolicitudes

import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.domain.MensajeSeguimiento
import com.example.gestionmovilsolicitudes.model.Categoria
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MensajeSeguimientoTest {

    private val solicitud = SolicitudEntity(
        id = 1,
        codigoSeguimiento = "SOL-2026-0005",
        clienteId = 1,
        titulo = "Sitio caído",
        descripcion = "texto privado de la descripción",
        categoria = Categoria.HOSTING
    )

    @Test
    fun incluyeCodigoTituloCategoriaYReferencia() {
        val mensaje = MensajeSeguimiento.construir(solicitud)

        assertTrue(mensaje.contains("SOL-2026-0005"))
        assertTrue(mensaje.contains("Sitio caído"))
        assertTrue(mensaje.contains("Hosting"))
        assertTrue(mensaje.contains(MensajeSeguimiento.enlace(solicitud)))
    }

    @Test
    fun noIncluyeLaDescripcion() {
        val mensaje = MensajeSeguimiento.construir(solicitud)

        assertFalse(mensaje.contains("texto privado"))
    }

    @Test
    fun elEnlaceTerminaEnElCodigoDeSeguimiento() {
        assertTrue(MensajeSeguimiento.enlace(solicitud).endsWith("SOL-2026-0005"))
    }
}
