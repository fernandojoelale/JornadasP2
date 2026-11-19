package ar.jornadas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ar.jornadas.Servicios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class ProgramaModel(
    app: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(app) {

    private val servicios = Servicios(app)
    val mesa = servicios.mesa
    private val repo = servicios.agenda

    val charlas = repo.programa.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dia: StateFlow<Int> = savedState.getStateFlow("filtro_dia", 0)
    val tema: StateFlow<String> = savedState.getStateFlow("filtro_tema", "Todos")

    private val _aviso = MutableStateFlow("")
    val aviso: StateFlow<String> = _aviso.asStateFlow()

    private val _mostrarReintentar = MutableStateFlow(false)
    val mostrarReintentar: StateFlow<Boolean> = _mostrarReintentar.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private var cargaInicialHecha = false

    init {
        if (!cargaInicialHecha) {
            cargaInicialHecha = true
            recargar()
        }
    }

    fun seleccionarDia(nuevoDia: Int) {
        savedState["filtro_dia"] = nuevoDia
    }

    fun seleccionarTema(nuevoTema: String) {
        savedState["filtro_tema"] = nuevoTema
    }

    fun recargar() {
        viewModelScope.launch {
            _cargando.value = true
            _aviso.value = ""
            _mostrarReintentar.value = false
            try {
                repo.sincronizar()
                _aviso.value = ""
                _mostrarReintentar.value = false
            } catch (_: IOException) {
                if (repo.tieneEdicionGuardada()) {
                    _aviso.value = "Sin conexión: se están usando datos guardados"
                    _mostrarReintentar.value = false
                } else {
                    _aviso.value = "No se pudo consultar el programa"
                    _mostrarReintentar.value = true
                }
            } catch (_: Exception) {
                if (repo.tieneEdicionGuardada()) {
                    _aviso.value = "Se están usando datos guardados"
                    _mostrarReintentar.value = false
                } else {
                    _aviso.value = "No se pudo consultar el programa"
                    _mostrarReintentar.value = true
                }
            } finally {
                _cargando.value = false
            }
        }
    }

    fun marcar(id: String) {
        viewModelScope.launch {
            repo.cambiarFavorita(id)
        }
    }
}