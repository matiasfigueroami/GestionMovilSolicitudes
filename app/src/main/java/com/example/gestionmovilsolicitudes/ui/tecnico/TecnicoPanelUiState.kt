package com.example.gestionmovilsolicitudes.ui.tecnico

import com.example.gestionmovilsolicitudes.data.dao.ActividadReciente
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud

data class TecnicoPanelUiState(
    val cargando: Boolean = true,
    val autorizado: Boolean = true,

    // ---- Indicadores de mis solicitudes ----
    val total: Int = 0,
    val atrasadas: Int = 0,
    val porEstado: List<Pair<EstadoSolicitud, Int>> = emptyList(),

    // ---- Lista (filtrable por estado) ----
    val filtroEstado: EstadoSolicitud? = null,
    val solicitudes: List<SolicitudEntity> = emptyList(),

    // ---- Historial de mis acciones ----
    val actividad: List<ActividadReciente> = emptyList()
)