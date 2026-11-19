package ar.jornadas.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AgendaRepository(
    context: Context,
    private val api: ProgramaApi,
    private val dao: CarteleraDao
) {
    private val prefs = context.applicationContext
        .getSharedPreferences("estado_programa", Context.MODE_PRIVATE)

    val programa = dao.observar()

    fun tieneEdicionGuardada(): Boolean = prefs.getBoolean("hay_edicion", false)

    private fun marcarEdicionGuardada() {
        prefs.edit().putBoolean("hay_edicion", true).apply()
    }

    suspend fun sincronizar() = withContext(Dispatchers.IO) {
        val remotas = api.consultar()
        val fichas = remotas.map {
            Charla(
                id = it.id,
                titulo = it.title,
                tema = it.track,
                dia = it.day,
                hora = it.time,
                sala = it.room,
                ponente = it.speaker,
                resumen = it.summary,
                favorita = false
            )
        }
        dao.aplicarInstantanea(fichas)
        marcarEdicionGuardada()
    }

    suspend fun cambiarFavorita(id: String) = withContext(Dispatchers.IO) {
        dao.alternar(id)
    }
}