package co.edu.udea.compumovil.gr05_20261.lab1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.edu.udea.compumovil.gr05_20261.lab1.ui.theme.LabsCM20261Gr05Theme

class ContactDataActivity : ComponentActivity() {

    private val viewModel: ContactDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Aquí leemos los datos que PersonalDataActivity nos envió
         * en el Intent con putExtra.
         * "?: """ significa "si el valor es null, usa cadena vacía".
         * Un Intent puede no tener el extra si algo salió mal,
         * así evitamos un NullPointerException.
         */
        val nombres = intent.getStringExtra("nombres") ?: ""
        val apellidos = intent.getStringExtra("apellidos") ?: ""
        val sexo = intent.getStringExtra("sexo") ?: ""
        val fechaNacimiento = intent.getStringExtra("fechaNacimiento") ?: ""
        val gradoEscolaridad = intent.getStringExtra("gradoEscolaridad") ?: ""

        setContent {
            LabsCM20261Gr05Theme {
                ContactDataScreen(
                    viewModel = viewModel,
                    onGuardar = {
                        // Al guardar, logueamos los datos de ambas pantallas
                        viewModel.logData(
                            nombres, apellidos, sexo,
                            fechaNacimiento, gradoEscolaridad
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDataScreen(
    viewModel: ContactDataViewModel,
    onGuardar: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusDireccion = remember { FocusRequester() }
    val focusEmail = remember { FocusRequester() }

    /**
     * derivedStateOf: calcula un valor basado en otro estado.
     * Solo se recalcula cuando viewModel.pais cambia.
     * Más eficiente que recalcular en cada recomposición.
     *
     * filter { it.contains(..., ignoreCase = true) }:
     * filtra la lista de países dejando solo los que contienen
     * lo que el usuario escribió, sin importar mayúsculas/minúsculas.
     */
    val paisesFiltrados by remember {
        derivedStateOf {
            if (viewModel.pais.length < 1) emptyList()
            else AppData.paisesLatinoamerica.filter {
                it.contains(viewModel.pais, ignoreCase = true)
            }
        }
    }

    val ciudadesFiltradas by remember {
        derivedStateOf {
            if (viewModel.ciudad.length < 1) emptyList()
            else AppData.ciudadesColombia.filter {
                it.contains(viewModel.ciudad, ignoreCase = true)
            }
        }
    }

    var expandedPais by remember { mutableStateOf(false) }
    var expandedCiudad by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_contact_data)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── TELÉFONO ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = viewModel.telefono,
                onValueChange = { viewModel.onTelefonoChange(it) },
                label = { Text(stringResource(R.string.label_telefono)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = viewModel.errorTelefono != null,
                supportingText = {
                    viewModel.errorTelefono?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    // KeyboardType.Phone: muestra teclado numérico de teléfono
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusDireccion.requestFocus() }
                )
            )

            // ── DIRECCIÓN ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = viewModel.direccion,
                onValueChange = { viewModel.onDireccionChange(it) },
                label = { Text(stringResource(R.string.label_direccion)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusDireccion),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    // autoCorrect = false: teclado sin sugerencias
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusEmail.requestFocus() }
                )
            )

            // ── EMAIL ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text(stringResource(R.string.label_email)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusEmail),
                singleLine = true,
                isError = viewModel.errorEmail != null,
                supportingText = {
                    viewModel.errorEmail?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    // KeyboardType.Email: teclado con @ visible y dominio fácil
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // ── PAÍS (Autocompletar) ──────────────────────────────────────────
            /**
             * Autocompletado = ExposedDropdownMenuBox donde el usuario
             * puede escribir libremente y el menú filtra las opciones
             * en tiempo real según lo que escribe.
             */
            ExposedDropdownMenuBox(
                expanded = expandedPais && paisesFiltrados.isNotEmpty(),
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.pais,
                    onValueChange = {
                        viewModel.onPaisChange(it)
                        expandedPais = true  // Abre el menú al escribir
                    },
                    label = { Text(stringResource(R.string.label_pais)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    isError = viewModel.errorPais != null,
                    supportingText = {
                        viewModel.errorPais?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.clearFocus() }
                    )
                )
                // El menú solo aparece si hay países que coincidan
                if (paisesFiltrados.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandedPais,
                        onDismissRequest = { expandedPais = false }
                    ) {
                        paisesFiltrados.forEach { pais ->
                            DropdownMenuItem(
                                text = { Text(pais) },
                                onClick = {
                                    viewModel.onPaisChange(pais)  // Completa el campo
                                    expandedPais = false           // Cierra el menú
                                }
                            )
                        }
                    }
                }
            }

            // ── CIUDAD (Autocompletar) ────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = expandedCiudad && ciudadesFiltradas.isNotEmpty(),
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.ciudad,
                    onValueChange = {
                        viewModel.onCiudadChange(it)
                        expandedCiudad = true
                    },
                    label = { Text(stringResource(R.string.label_ciudad)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                if (ciudadesFiltradas.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandedCiudad,
                        onDismissRequest = { expandedCiudad = false }
                    ) {
                        ciudadesFiltradas.forEach { ciudad ->
                            DropdownMenuItem(
                                text = { Text(ciudad) },
                                onClick = {
                                    viewModel.onCiudadChange(ciudad)
                                    expandedCiudad = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── BOTÓN GUARDAR ─────────────────────────────────────────────────
            Button(
                onClick = {
                    if (viewModel.validar()) {
                        onGuardar()  // Llama a logData() con todos los datos
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_guardar))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
