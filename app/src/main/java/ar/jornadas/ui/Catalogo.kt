package ar.jornadas.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.jornadas.data.Charla

@Composable
fun Catalogo(model: ProgramaModel, abrir: (String) -> Unit) {
    val dia by model.dia.collectAsStateWithLifecycle()
    val tema by model.tema.collectAsStateWithLifecycle()
    val fichas by model.charlas.collectAsStateWithLifecycle()
    val aviso by model.aviso.collectAsStateWithLifecycle()
    val mostrarReintentar by model.mostrarReintentar.collectAsStateWithLifecycle()
    val cargando by model.cargando.collectAsStateWithLifecycle()

    Selector(listOf("Ambos días", "Día 1", "Día 2"), dia) { model.seleccionarDia(it) }

    val temas = listOf("Todos", "Android", "Datos", "Calidad")
    val indiceTema = temas.indexOf(tema).coerceAtLeast(0)
    Selector(temas, indiceTema) { model.seleccionarTema(temas[it]) }

    TextButton(onClick = model::recargar) { Text("Actualizar") }

    if (cargando) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
    }

    if (aviso.isNotEmpty()) {
        Text(aviso)
        if (mostrarReintentar) {
            OutlinedButton(onClick = model::recargar) { Text("Reintentar") }
        }
    }

    val filtradas = fichas.filter {
        (dia == 0 || it.dia == dia) && (tema == "Todos" || it.tema == tema)
    }

    if (filtradas.isEmpty() && !mostrarReintentar && !cargando) {
        Text("sin charlas programadas")
    } else if (!mostrarReintentar || fichas.isNotEmpty()) {
        Grilla(filtradas, abrir)
    }
}

@Composable
private fun Selector(opciones: List<String>, indice: Int, elegir: (Int) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        opciones.forEachIndexed { i, texto ->
            FilterChip(
                selected = i == indice,
                onClick = { elegir(i) },
                label = { Text(texto) }
            )
        }
    }
}

@Composable
fun Grilla(fichas: List<Charla>, abrir: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        items(fichas, key = { it.id }) { ficha ->
            ElevatedCard(onClick = { abrir(ficha.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Día ${ficha.dia} · ${ficha.hora} · ${ficha.sala}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(ficha.titulo, style = MaterialTheme.typography.titleLarge)
                    Text(if (ficha.favorita) "${ficha.tema} · En mi agenda" else ficha.tema)
                }
            }
        }
    }
}