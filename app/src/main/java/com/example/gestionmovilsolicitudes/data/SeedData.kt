package com.example.gestionmovilsolicitudes.data

import com.example.gestionmovilsolicitudes.data.entity.ClienteEntity
import com.example.gestionmovilsolicitudes.data.entity.HistorialEntity
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad

/**
 * Datos 100% ficticios para la demo. Cuentas de prueba (password "1234"):
 * admin@demo.cl, tecnico1@demo.cl, tecnico2@demo.cl, cliente1@demo.cl, cliente2@demo.cl
 */
object SeedData {
    private const val DIA = 24L * 60 * 60 * 1000

    suspend fun sembrarSiVacia(db: AppDatabase) {
        if (db.usuarioDao().contar() > 0) return

        val ahora = System.currentTimeMillis()
        val pass = "1234"

        val cli1 = db.clienteDao().insertar(
            ClienteEntity(
                nombreCliente = "Cliente Uno", nombreEmpresa = "Empresa Demo Uno",
                correo = "cliente1@demo.cl", telefono = "+56 9 0000 0001",
                serviciosAsociados = "Sitio web, Hosting"
            )
        )
        val cli2 = db.clienteDao().insertar(
            ClienteEntity(
                nombreCliente = "Cliente Dos", nombreEmpresa = "Empresa Demo Dos",
                correo = "cliente2@demo.cl", telefono = "+56 9 0000 0002",
                serviciosAsociados = "Correo corporativo"
            )
        )

        val admin = db.usuarioDao().insertar(
            UsuarioEntity(nombre = "Admin Demo", correo = "admin@demo.cl", password = pass, perfil = Perfil.ADMINISTRADOR)
        )
        val tec1 = db.usuarioDao().insertar(
            UsuarioEntity(nombre = "Tecnico Uno", correo = "tecnico1@demo.cl", password = pass, perfil = Perfil.TECNICO)
        )
        db.usuarioDao().insertar(
            UsuarioEntity(nombre = "Tecnico Dos", correo = "tecnico2@demo.cl", password = pass, perfil = Perfil.TECNICO)
        )
        val uCli1 = db.usuarioDao().insertar(
            UsuarioEntity(
                nombre = "Cliente Uno", correo = "cliente1@demo.cl", password = pass,
                perfil = Perfil.CLIENTE, clienteId = cli1, empresaAsociada = "Empresa Demo Uno"
            )
        )
        val uCli2 = db.usuarioDao().insertar(
            UsuarioEntity(
                nombre = "Cliente Dos", correo = "cliente2@demo.cl", password = pass,
                perfil = Perfil.CLIENTE, clienteId = cli2, empresaAsociada = "Empresa Demo Dos"
            )
        )

        suspend fun solicitud(
            codigo: String, clienteId: Long, creador: Long, titulo: String, desc: String,
            categoria: Categoria, estado: EstadoSolicitud, prioridad: Prioridad?,
            responsable: Long?, hace: Long
        ) {
            val creada = ahora - hace * DIA
            val id = db.solicitudDao().insertar(
                SolicitudEntity(
                    codigoSeguimiento = codigo, clienteId = clienteId, titulo = titulo,
                    descripcion = desc, categoria = categoria, prioridad = prioridad,
                    estado = estado, responsableId = responsable,
                    fechaCreacion = creada, fechaActualizacion = ahora,
                )
            )
            db.historialDao().insertar(
                HistorialEntity(
                    solicitudId = id, estadoAnterior = null, estadoNuevo = EstadoSolicitud.RECIBIDA,
                    accionRealizada = "Solicitud creada", perfilQueRealizoAccion = Perfil.CLIENTE,
                    usuarioResponsableId = creador, fechaHora = creada
                )
            )
            if (estado != EstadoSolicitud.RECIBIDA) {
                db.historialDao().insertar(
                    HistorialEntity(
                        solicitudId = id, estadoAnterior = EstadoSolicitud.RECIBIDA, estadoNuevo = estado,
                        accionRealizada = "Cambio de estado (datos de ejemplo)",
                        perfilQueRealizoAccion = Perfil.ADMINISTRADOR, usuarioResponsableId = admin,
                        fechaHora = ahora
                    )
                )
            }
        }

        solicitud("SOL-2026-0001", cli1, uCli1, "Cambiar banner de portada", "Actualizar imagen principal del sitio.",
            Categoria.MANTENIMIENTO_WEB, EstadoSolicitud.RECIBIDA, null, null, 1)
        solicitud("SOL-2026-0002", cli1, uCli1, "Sitio caido", "El sitio no carga desde la manana.",
            Categoria.INCIDENTE_TECNICO, EstadoSolicitud.EN_PROCESO, Prioridad.URGENTE, tec1, 8)
        solicitud("SOL-2026-0003", cli2, uCli2, "Crear casilla de correo", "Necesito una casilla nueva.",
            Categoria.SOPORTE_CORREO, EstadoSolicitud.PENDIENTE_INFORMACION, Prioridad.MEDIA, null, 3)
        solicitud("SOL-2026-0004", cli2, uCli2, "Consulta por renovacion de hosting", "Cuando vence mi plan?",
            Categoria.HOSTING, EstadoSolicitud.RESUELTA, Prioridad.BAJA, tec1, 5)
    }
}
