package ar.jornadas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CarteleraDao {
    @Query("SELECT * FROM charlas ORDER BY dia, hora, sala")
    fun observar(): Flow<List<Charla>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarNuevas(fichas: List<Charla>): List<Long>

    @Query(
        """
        UPDATE charlas
        SET titulo = :titulo,
            tema = :tema,
            dia = :dia,
            hora = :hora,
            sala = :sala,
            ponente = :ponente,
            resumen = :resumen
        WHERE id = :id
        """
    )
    suspend fun actualizarDatosCharla(
        id: String,
        titulo: String,
        tema: String,
        dia: Int,
        hora: String,
        sala: String,
        ponente: String,
        resumen: String
    )

    @Query("DELETE FROM charlas WHERE id NOT IN (:idsVigentes)")
    suspend fun eliminarAusentes(idsVigentes: List<String>)

    @Query("DELETE FROM charlas")
    suspend fun vaciarCharlas()

    @Transaction
    suspend fun aplicarInstantanea(fichas: List<Charla>) {
        if (fichas.isEmpty()) {
            vaciarCharlas()
            return
        }
        insertarNuevas(fichas)
        for (ficha in fichas) {
            actualizarDatosCharla(
                id = ficha.id,
                titulo = ficha.titulo,
                tema = ficha.tema,
                dia = ficha.dia,
                hora = ficha.hora,
                sala = ficha.sala,
                ponente = ficha.ponente,
                resumen = ficha.resumen
            )
        }
        eliminarAusentes(fichas.map { it.id })
    }

    @Query("UPDATE charlas SET favorita = NOT favorita WHERE id = :id")
    suspend fun alternar(id: String)
}