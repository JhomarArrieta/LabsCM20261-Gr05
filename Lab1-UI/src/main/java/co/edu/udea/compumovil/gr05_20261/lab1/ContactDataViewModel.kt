package co.edu.udea.compumovil.gr05_20261.lab1

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ContactDataViewModel : ViewModel() {

    // ── Datos del formulario ──────────────────────────────────────────────────
    var telefono by mutableStateOf("")
        private set

    var direccion by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var pais by mutableStateOf("")
        private set

    var ciudad by mutableStateOf("")
        private set

    // ── Errores de validación ─────────────────────────────────────────────────
    var errorTelefono by mutableStateOf<String?>(null)
        private set

    var errorEmail by mutableStateOf<String?>(null)
        private set

    var errorPais by mutableStateOf<String?>(null)
        private set

    // ── Funciones para actualizar desde la pantalla ───────────────────────────
    fun onTelefonoChange(value: String) {
        telefono = value
        if (value.isNotBlank()) errorTelefono = null
    }

    fun onDireccionChange(value: String) { direccion = value }

    fun onEmailChange(value: String) {
        email = value
        if (value.isNotBlank()) errorEmail = null
    }

    fun onPaisChange(value: String) {
        pais = value
        if (value.isNotBlank()) errorPais = null
    }

    fun onCiudadChange(value: String) { ciudad = value }

    /**
     * Valida campos obligatorios.
     * "Patterns.EMAIL_ADDRESS" es una utilidad de Android
     * que valida si un email tiene el formato correcto (tiene @ y dominio).
     */
    fun validar(): Boolean {
        var esValido = true

        if (telefono.isBlank()) {
            errorTelefono = "Este campo es obligatorio"
            esValido = false
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail = "Email inválido o vacío"
            esValido = false
        }
        if (pais.isBlank()) {
            errorPais = "Este campo es obligatorio"
            esValido = false
        }

        return esValido
    }

    /**
     * Loguea TODO (datos personales + contacto) en Logcat.
     * Recibe los datos personales como parámetros porque vienen
     * del Intent que mandó PersonalDataActivity.
     */
    fun logData(
        nombres: String,
        apellidos: String,
        sexo: String,
        fechaNacimiento: String,
        gradoEscolaridad: String
    ) {
        val sexoLog = sexo.ifBlank { "(no especificado)" }
        val gradoLog = gradoEscolaridad.ifBlank { "(no especificado)" }
        val dirLog = direccion.ifBlank { "(no especificada)" }
        val ciudadLog = ciudad.ifBlank { "(no especificada)" }

        android.util.Log.i(
            "LAB1_PersonalData",
            "Información personal: $nombres $apellidos $sexoLog " +
                    "Nació el $fechaNacimiento $gradoLog"
        )
        android.util.Log.i(
            "LAB1_ContactData",
            "Información de contacto:\n" +
                    "Teléfono: $telefono\n" +
                    "Dirección: $dirLog\n" +
                    "Email: $email\n" +
                    "País: $pais\n" +
                    "Ciudad: $ciudadLog"
        )
    }
}