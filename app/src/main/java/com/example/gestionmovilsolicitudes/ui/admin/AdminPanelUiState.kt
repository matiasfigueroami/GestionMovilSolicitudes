package com.example.gestionmovilsolicitudes.ui.admin

import com.example.gestionmovilsolicitudes.data.entity.ClienteEntity
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Prioridad

data class AdminPanelUiState(
    val cargando: Boolean = true,
    val autorizado: Boolean = true,

    // ---- Resumen ----
    val total: Int = 0,
    val atrasadas: Int = 0,
    val porEstado: List<Pair<EstadoSolicitud, Int>> = emptyList(),
    val porPrioridad: List<Pair<String, Int>> = emptyList(),
    val porResponsable: List<Pair<String, Int>> = emptyList(),

    // ---- Filtros aplicados ----
    val filtroEstado: EstadoSolicitud? = null,
    val filtroPrioridad: Prioridad? = null,
    val filtroClienteId: Long? = null,
    val filtroResponsableId: Long? = null,
    val texto: String = "",
    val soloAtrasadas: Boolean = false,

    // ---- Opciones de los filtros y resultado ----
    val clientes: List<ClienteEntity> = emptyList(),
    val tecnicos: List<UsuarioEntity> = emptyList(),
    val solicitudes: List<SolicitudEntity> = emptyList()
) {
    val hayFiltros: Boolean
        get() = filtroEstado != null || filtroPrioridad != null || filtroClienteId != null ||
                filtroResponsableId != null || texto.isNotBlank() || soloAtrasadas
}