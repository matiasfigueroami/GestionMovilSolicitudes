package com.example.gestionmovilsolicitudes.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionmovilsolicitudes.GestionApp
import com.example.gestionmovilsolicitudes.data.dao.ConteoEstado
import com.example.gestionmovilsolicitudes.data.dao.ConteoPrioridad
import com.example.gestionmovilsolicitudes.data.dao.ConteoResponsable
import com.example.gestionmovilsolicitudes.domain.ReglasAtraso
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad
import com.example.gestionmovilsolicitudes.model.etiqueta
import com.example.gestionmovilsolicitudes.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class AdminPanelViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as GestionApp).repository

    private data class Filtros(
        val estado: EstadoSolicitud? = null,
        val prioridad: Prioridad? = null,
        val clienteId: Long? = null,
        val responsableId: Long? = null,
        val texto: String = "",
        val soloAtrasadas: Boolean = false
    )

    private data class Resumen(
        val estados: List<ConteoEstado>,
        val prioridades: List<ConteoPrioridad>,
        val responsables: List<ConteoResponsable>,
        val atrasadas: Int
    )

    private val filtros = MutableStateFlow(Filtros())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AdminPanelUiState> = SessionManager.usuario
        .flatMapLatest { usuario ->
            if (usuario == null || usuario.perfil != Perfil.ADMINISTRADOR) {
                flowOf(AdminPanelUiState(cargando = false, autorizado = false))
            } else {
                // Cantidades: salen de consultas COUNT sobre Room
                val resumen = combine(
                    repo.conteoPorEstado(usuario),
                    repo.conteoPorPrioridad(usuario),
                    repo.conteoPorResponsable(usuario),
                    repo.observarAtrasadas(usuario, ReglasAtraso.limite())
                ) { e, p, r, a -> Resumen(e, p, r, a.size) }

                // Lista: se vuelve a consultar cada vez que cambia un filtro
                val lista = filtros.flatMapLatest { f ->
                    repo.filtrar(
                        usuario, f.estado, f.prioridad, f.clienteId, f.responsableId,
                        normalizarBusqueda(f.texto)
                    ).map { l -> if (f.soloAtrasadas) l.filter { ReglasAtraso.esAtrasada(it) } else l }
                }

                combine(
                    resumen, lista, repo.observarClientes(usuario), repo.observarTecnicos(), filtros
                ) { r, l, clientes, tecnicos, f ->
                    AdminPanelUiState(
                        cargando = false,
                        total = r.estados.sumOf { it.total },
                        atrasadas = r.atrasadas,
                        porEstado = EstadoSolicitud.entries.map { est ->
                            est to (r.estados.firstOrNull { it.estado == est }?.total ?: 0)
                        },
                        porPrioridad = Prioridad.entries.map { p ->
                            p.etiqueta() to (r.prioridades.firstOrNull { it.prioridad == p }?.total ?: 0)
                        } + ("Sin definir" to (r.prioridades.firstOrNull { it.prioridad == null }?.total ?: 0)),
                        porResponsable = r.responsables.map { c ->
                            val nombre = tecnicos.firstOrNull { it.id == c.responsableId }?.nombre ?: "Sin asignar"
                            nombre to c.total
                        }.sortedByDescending { it.second },
                        filtroEstado = f.estado,
                        filtroPrioridad = f.prioridad,
                        filtroClienteId = f.clienteId,
                        filtroResponsableId = f.responsableId,
                        texto = f.texto,
                        soloAtrasadas = f.soloAtrasadas,
                        clientes = clientes,
                        tecnicos = tecnicos,
                        solicitudes = l
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminPanelUiState())

    // "SOL-0002" o "sol-2" se buscan por su número, porque el código real es SOL-2026-0002
    private fun normalizarBusqueda(texto: String): String? {
        val t = texto.trim()
        val soloNumero = Regex("""^sol-?(\d+)$""", RegexOption.IGNORE_CASE).matchEntire(t)
        return (soloNumero?.groupValues?.get(1)?.padStart(4, '0') ?: t).ifBlank { null }
    }

    // ---------- Eventos de los filtros ----------
    fun onEstado(v: EstadoSolicitud?) = filtros.update { it.copy(estado = v) }
    fun onPrioridad(v: Prioridad?) = filtros.update { it.copy(prioridad = v) }
    fun onCliente(v: Long?) = filtros.update { it.copy(clienteId = v) }
    fun onResponsable(v: Long?) = filtros.update { it.copy(responsableId = v) }
    fun onTexto(v: String) = filtros.update { it.copy(texto = v) }
    fun onSoloAtrasadas(v: Boolean) = filtros.update { it.copy(soloAtrasadas = v) }
    fun limpiarFiltros() { filtros.value = Filtros() }
}