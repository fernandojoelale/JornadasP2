# Registro de cambios — Jornadas del Sur

Resumen de las modificaciones realizadas sobre Jornadas del Sur para cubrir los requisitos de mantenimiento.

---

## 1. Sincronización atómica y preservación de favoritas (`CarteleraDao.kt` y `AgendaRepository.kt`)

### `app/src/main/java/ar/jornadas/data/CarteleraDao.kt`
* Se reemplazó el guardado simple con `OnConflictStrategy.REPLACE` por `aplicarInstantanea(fichas: List<Charla>)` anotada con `@Transaction`:
    * Inserta las charlas nuevas con `OnConflictStrategy.IGNORE` (`favorita = false` por defecto).
    * Actualiza los campos descriptivos (`titulo`, `tema`, `dia`, `hora`, `sala`, `ponente`, `resumen`) de las charlas vigentes **sin sobrescribir** la columna `favorita`.
    * Elimina las charlas ausentes de la nueva instantánea (`DELETE FROM charlas WHERE id NOT IN (:idsVigentes)`), o vacía la tabla completa (`vaciarCharlas()`) si la respuesta viene vacía (`v4`).
    * Si una charla retirada reaparece en una instantánea posterior, entra como nueva con `favorita = false`.

### `app/src/main/java/ar/jornadas/data/AgendaRepository.kt`
* Se agregó persistencia en `SharedPreferences` (`"estado_programa"`, clave `"hay_edicion"`) mediante `tieneEdicionGuardada()` y `marcarEdicionGuardada()`.
* `marcarEdicionGuardada()` se ejecuta **solo** cuando `api.consultar()` y `dao.aplicarInstantanea(fichas)` completan sin excepción, registrando como edición válida incluso una respuesta exitosa con `0` charlas (`v4`).

---

## 2. Timeouts de red (`Servicios.kt`)

### `app/src/main/java/ar/jornadas/Servicios.kt`
* Se configuraron timeouts de conexión, lectura y escritura de `15` segundos en `OkHttpClient` para evitar cortes prematuros al probar el modo de demora larga (`8` segundos) en `ServicioLocal`.

---

## 3. Estado persistente, modo sin señal y control de descargas (`ProgramaModel.kt`, `Catalogo.kt` y `MainActivity.kt`)

### `app/src/main/java/ar/jornadas/ui/ProgramaModel.kt`
* Se migraron `dia` (`"filtro_dia"`) y `tema` (`"filtro_tema"`) a `SavedStateHandle` (`getStateFlow`), conservando los filtros ante rotaciones y recreación tras matar el proceso (`adb shell am kill ar.jornadas`), e iniciando limpios en una tarea nueva (`pm clear`).
* Se diferenció el manejo de errores (`IOException` y `Exception`):
    * **Con edición previa (`tieneEdicionGuardada() == true`):** mantiene la grilla, el detalle y las favoritas, e informa que se están usando datos guardados sin bloquear la vista con reintento.
    * **Sin edición previa (`tieneEdicionGuardada() == false`):** informa que no se pudo consultar el programa y habilita **Reintentar**.
* La carga automática se limitó a **una sola vez por instancia/sesión de proceso** (`cargaInicialHecha`), evitando recargas automáticas al rotar, cambiar de pestaña o entrar/salir del detalle, y permitiendo cambiar filtros mientras una petición está en curso.

### `app/src/main/java/ar/jornadas/ui/Catalogo.kt`
* Se conectaron los filtros (`dia` y `tema`), el indicador de carga (`cargando`), los avisos (`aviso`), el botón **Reintentar** (`mostrarReintentar`) y la grilla (`Grilla`).
* Cuando no hay error bloqueante ni carga activa y el resultado filtrado está vacío (como en `v4` o al aplicar un filtro sin coincidencias), muestra `"sin charlas programadas"`.

### `app/src/main/java/ar/jornadas/MainActivity.kt`
* Se eliminaron recargas automáticas ligadas al ciclo de vida (`onStart` / composiciones), manteniendo la lectura inicial del extra `servicio_disponible` cuando `savedInstanceState == null`.

---

## 4. Verificación realizada
* Compilación del APK de debug (`assembleDebug`) generando `app/build/outputs/apk/debug/app-debug.apk`.
* Comprobación de escenarios sin señal con y sin edición previa, programa vacío `v4` y reapertura sin señal, transición de versiones (`v1` -> `v2` -> `v3`) con altas/bajas/favoritas, preservación de filtros y detalle ante rotación y `am kill`, y conteo de peticiones en `Mesa`.

