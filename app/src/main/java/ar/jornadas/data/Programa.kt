package ar.jornadas.data

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

data class FichaRemota(
    val id: String,
    val title: String,
    val track: String,
    val day: Int,
    val time: String,
    val room: String,
    val speaker: String,
    val summary: String
)

interface ProgramaApi {
    @GET("programa")
    suspend fun consultar(): List<FichaRemota>
}

@Entity(tableName = "charlas")
data class Charla(
    @PrimaryKey val id: String,
    val titulo: String,
    val tema: String,
    val dia: Int,
    val hora: String,
    val sala: String,
    val ponente: String,
    val resumen: String,
    val favorita: Boolean = false
)

@Database(entities = [Charla::class], version = 1, exportSchema = false)
abstract class Archivo : RoomDatabase() {
    abstract fun cartelera(): CarteleraDao
}