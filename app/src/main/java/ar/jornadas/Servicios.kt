package ar.jornadas

import android.content.Context
import androidx.room.Room
import ar.jornadas.data.AgendaRepository
import ar.jornadas.data.Archivo
import ar.jornadas.data.ProgramaApi
import ar.jornadas.lab.ServicioLocal
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Servicios(context: Context) {
    private val appContext = context.applicationContext
    val mesa = ServicioLocal(appContext)
    private val archivo = Room.databaseBuilder(appContext, Archivo::class.java, "cartelera.db").build()
    private val cliente = OkHttpClient.Builder()
        .addInterceptor(mesa)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    private val api = Retrofit.Builder()
        .baseUrl("https://programa.invalid/")
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ProgramaApi::class.java)

    val agenda = AgendaRepository(appContext, api, archivo.cartelera())
}