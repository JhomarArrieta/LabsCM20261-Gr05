package co.edu.udea.compumovil.gr05_20261.lab1

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel: sobrevive cuando el usuario rota la pantalla.
 *
 * Sin ViewModel: el usuario rota el celular → la Activity se destruye
 * y se recrea → todo lo que escribió se borra.
 *
 * Con ViewModel: los datos viven más que la Activity.
 * Android los mantiene vivos durante la rotación.
 *
 * "mutableStateOf": es una variable especial de Jetpack Compose.
 * Cuando su valor cambia, Compose automáticamente redibuja
 * los componentes de la pantalla que la usan. No tienes que
 * llamar a nada manualmente.
 *
 * "by": es un delegado de Kotlin. Hace que puedas escribir
 * "nombres" en vez de "nombres.value" cada vez que lo usas.
 *
 * "private set": solo el ViewModel puede modificar la variable.
 * La Activity solo puede leerla, no cambiarla directamente.
 * Para cambiarla, la Activity llama a las funciones "onXxxChange".
 */
class PersonalDataViewModel : ViewModel() {

    // ── Datos del formulario ──────────────────────────────────────────────────
    var nombres by mutableStateOf("")
        private set

    var apellidos by mutableStateOf("")
        private set

    // "" = ningún sexo seleccionado todavía
    var sexo by mutableStateOf("")
        private set

    // Guardamos la fecha como texto "dd/M/yyyy", ej: "20/5/1999"
    var fechaNacimiento by mutableStateOf("")
        private set

    var gradoEscolaridad by mutableStateOf("")
        private set

    // ── Mensajes de error para cada campo obligatorio ─────────────────────────
    // String? = puede ser null.
    // null = sin error (todo bien)
    // "Este campo es obligatorio" = hay un error que mostrar
    var errorNombres by mutableStateOf<String?>(null)
        private set

    var errorApellidos by mutableStateOf<String?>(null)
        private set

    var errorFecha by mutableStateOf<String?>(null)
        private set

    // ── Funciones para actualizar los datos desde la pantalla ─────────────────
    fun onNombresChange(value: String) {
        nombres = value
        // Si el usuario empieza a escribir, quitamos el error
        if (value.isNotBlank()) errorNombres = null
    }

    fun onApellidosChange(value: String) {
        apellidos = value
        if (value.isNotBlank()) errorApellidos = null
    }

    fun onSexoChange(value: String) {
        sexo = value
    }

    fun onFechaNacimientoChange(value: String) {
        fechaNacimiento = value
        if (value.isNotBlank()) errorFecha = null
    }

    fun onGradoEscolaridadChange(value: String) {
        gradoEscolaridad = value
    }

    /**
     * Valida que los campos obligatorios (*) estén llenos.
     * Retorna true si todo está bien, false si hay errores.
     * Los errores se muestran solos en la UI porque son mutableStateOf.
     */
    fun validar(): Boolean {
        var esValido = true

        if (nombres.isBlank()) {
            errorNombres = "Este campo es obligatorio"
            esValido = false
        }
        if (apellidos.isBlank()) {
            errorApellidos = "Este campo es obligatorio"
            esValido = false
        }
        if (fechaNacimiento.isBlank()) {
            errorFecha = "Este campo es obligatorio"
            esValido = false
        }

        return esValido
    }

    /**
     * Imprime los datos en Logcat.
     * En Android Studio los ves en la pestaña "Logcat"
     * filtrando por la etiqueta "LAB1_PersonalData".
     */
    fun logData() {
        // ifBlank{} = si está vacío, usa este texto por defecto
        val sexoLog = sexo.ifBlank { "(no especificado)" }
        val gradoLog = gradoEscolaridad.ifBlank { "(no especificado)" }

        android.util.Log.i(
            "LAB1_PersonalData",
            "Información personal: $nombres $apellidos $sexoLog " +
                    "Nació el $fechaNacimiento $gradoLog"
        )
    }
}