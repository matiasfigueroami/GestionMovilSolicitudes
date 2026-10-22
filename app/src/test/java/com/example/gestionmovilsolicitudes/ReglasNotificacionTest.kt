package com.example.gestionmovilsolicitudes

import com.example.gestionmovilsolicitudes.data.dao.NovedadHistorial
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.domain.ReglasNotificacion
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasNotificacionTest {

    private val solicitud = SolicitudEntity(
        id = 7,
        codigoSeguimiento = "SOL-2026-0007",
        clienteId = 1,
        titulo = "Sitio caído",
        descripcion = "Detalle",
        categoria = Categoria.HOSTING
    )

    @Test
    fun solicitudResueltaAvisaAlClienteConElCodigo() {
        val aviso = ReglasNotificacion.cambioEstado(solicitud, EstadoSolicitud.RESUELTA, Perfil.TECNICO)

        assertNotNull(aviso)
        assertEquals(Perfil.CLIENTE, aviso!!.para)
        assertTrue(aviso.texto.contains("SOL-2026-0007"))
    }

    @Test
    fun respuestaDelClienteAvisaAlAdministrador() {
        val aviso = ReglasNotificacion.cambioEstado(solicitud, EstadoSolicitud.EN_REVISION, Perfil.CLIENTE)

        assertEquals(Perfil.ADMINISTRADOR, aviso!!.para)
    }

    @Test
    fun enProcesoDependeDeQuienLoCausa() {
        val reapertura = ReglasNotificacion.cambioEstado(solicitud, EstadoSolicitud.EN_PROCESO, Perfil.CLIENTE)
        val inicio = ReglasNotificacion.cambioEstado(solicitud, EstadoSolicitud.EN_PROCESO, Perfil.TECNICO)

        assertEquals(Perfil.ADMINISTRADOR, reapertura!!.para)
        assertEquals(Perfil.CLIENTE, inicio!!.para)
    }

    @Test
    fun estadoRecibidaNoGeneraAviso() {
        assertNull(ReglasNotificacion.cambioEstado(solicitud, EstadoSolicitud.RECIBIDA, Perfil.CLIENTE))
    }

    @Test
    fun comentarioAvisaALaOtraParte() {
        assertEquals(Perfil.ADMINISTRADOR, ReglasNotificacion.comentario(solicitud, Perfil.CLIENTE).para)
        assertEquals(Perfil.CLIENTE, ReglasNotificacion.comentario(solicitud, Perfil.TECNICO).para)
    }

    @Test
    fun avisoDeAtrasadasIncluyeLaCantidad() {
        val aviso = ReglasNotificacion.atrasadas(3)

        assertEquals(Perfil.ADMINISTRADOR, aviso.para)
        assertTrue(aviso.texto.contains("3"))
    }

    private fun novedad(id: Long, accion: String, comentario: String? = null) =
        NovedadHistorial(id, 7, "SOL-2026-0007", "Sitio caído", accion, comentario, 1000L + id)

    @Test
    fun sinNovedadesNoHayAvisos() {
        assertTrue(ReglasNotificacion.novedades(Perfil.CLIENTE, emptyList()).isEmpty())
    }

    @Test
    fun pocasNovedadesGeneranUnAvisoCadaUnaConElComentario() {
        val avisos = ReglasNotificacion.novedades(
            Perfil.CLIENTE,
            listOf(novedad(2, "Solución registrada", "Se reinició el servidor"), novedad(1, "Trabajo iniciado"))
        )

        assertEquals(2, avisos.size)
        assertEquals("Trabajo iniciado", avisos[0].titulo) // se publican de la más antigua a la más reciente
        assertTrue(avisos[1].texto.contains("Se reinició el servidor"))
        assertTrue(avisos.all { it.para == Perfil.CLIENTE })
    }

    @Test
    fun muchasNovedadesSeResumenEnUnSoloAviso() {
        val avisos = ReglasNotificacion.novedades(
            Perfil.TECNICO, (1L..5L).map { novedad(it, "Cambio $it") }
        )

        assertEquals(1, avisos.size)
        assertTrue(avisos[0].titulo.contains("5"))
    }
}