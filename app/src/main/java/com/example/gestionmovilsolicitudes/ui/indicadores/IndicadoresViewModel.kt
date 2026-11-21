package com.example.gestionmovilsolicitudes.ui.indicadores

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionmovilsolicitudes.GestionApp
import com.example.gestionmovilsolicitudes.data.repository.Indicador
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IndicadoresUiState(
    val cargando: Boolean = true,
    val indicadores: List<Indicador> = emptyList(),
    val error: String? = null
)

class IndicadoresViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as GestionApp).indicadoresRepository

    private val _uiState = MutableStateFlow(IndicadoresUiState())
    val uiState: StateFlow<IndicadoresUiState> = _uiState.asStateFlow()

    private var carga: Job? = null

    init {
        cargar()
    }

    /** GET https://mindicador.cl/api con Retrofit. */
    fun cargar() {
        if (carga?.isActive == true) return
        carga = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val lista = repo.obtener()
                _uiState.update { it.copy(cargando = false, indicadores = lista) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron cargar los indicadores (revisa la conexión a internet)")
                }
            }
        }
    }
}
