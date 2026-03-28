package co.edu.udea.compumovil.gr05_20261.lab1

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.edu.udea.compumovil.gr05_20261.lab1.ui.theme.LabsCM20261Gr05Theme
import java.util.Calendar
import java.util.TimeZone
import kotlin.jvm.java

class PersonalDataActivity : ComponentActivity() {

    /**
     * "by viewModels()" crea el ViewModel y lo vincula al ciclo de vida
     * de esta Activity. Android se encarga de mantenerlo vivo durante
     * rotaciones y de destruirlo cuando la Activity se cierra de verdad.
     */
    private val viewModel: PersonalDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * setContent{} reemplaza completamente el uso de archivos XML.
         * Aquí defines toda la UI de esta pantalla usando Compose.
         * Cada vez que un mutableStateOf del ViewModel cambia,
         * Compose redibuja solo los componentes afectados.
         */
        setContent {
            LabsCM20261Gr05Theme {
                PersonalDataScreen(
                    viewModel = viewModel,
                    onSiguiente = {
                        /**
                         * Intent = mensaje para abrir otra Activity.
                         * "putExtra" = adjuntamos datos al mensaje,
                         * como pasar parámetros a una función.
                         * ContactDataActivity los leerá con "getStringExtra".
                         */
                        val intent = Intent(this, ContactDataActivity::class.java).apply {
                            putExtra("nombres", viewModel.nombres)
                            putExtra("apellidos", viewModel.apellidos)
                            putExtra("sexo", viewModel.sexo)
                            putExtra("fechaNacimiento", viewModel.fechaNacimiento)
                            putExtra("gradoEscolaridad", viewModel.gradoEscolaridad)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

/**
 * @Composable = función que dibuja UI. Compose la llama cada vez que
 * algún estado que usa cambia. Es como "observar" los datos.
 *
 * @OptIn(ExperimentalMaterial3Api::class) = algunos componentes de
 * Material3 son experimentales. Esta anotación le dice al compilador
 * que sabemos que pueden cambiar en versiones futuras y aceptamos el riesgo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDataScreen(
    viewModel: PersonalDataViewModel,
    onSiguiente: () -> Unit
) {
    /**
     * FocusRequester: permite mover el foco (cursor del teclado)
     * a un campo específico programáticamente.
     * Cuando el usuario presiona "Siguiente" en el teclado de Nombres,
     * el foco salta a Apellidos automáticamente.
     *
     * "remember": guarda el valor entre recomposiciones.
     * Sin "remember", se crearía un nuevo FocusRequester cada vez
     * que Compose redibuja, perdiendo la referencia al campo.
     */
    val focusApellidos = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Estado para mostrar/ocultar el diálogo del DatePicker
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Estado para abrir/cerrar el dropdown del Spinner
    var expandedSpinner by remember { mutableStateOf(false) }

    // Lee el array de strings.xml — cambia automáticamente con el idioma del teléfono
    val gradosEscolaridad = stringArrayResource(R.array.grados_escolaridad).toList()

    // Las opciones de sexo también vienen de strings.xml para soportar multilenguaje
    val sexoOptions = listOf(
        stringResource(R.string.option_masculino),
        stringResource(R.string.option_femenino),
        stringResource(R.string.option_otro)
    )

    // ── Diálogo del DatePicker ────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        /**
                         * El DatePicker devuelve milisegundos desde 1970 (Unix timestamp).
                         * Usamos Calendar con zona horaria UTC para convertirlo
                         * a día/mes/año sin problemas de huso horario.
                         * Sin UTC, en Colombia (UTC-5) la fecha podría
                         * quedar un día antes de la seleccionada.
                         */
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        val dia = cal.get(Calendar.DAY_OF_MONTH)
                        val mes = cal.get(Calendar.MONTH) + 1 // Enero = 0, por eso sumamos 1
                        val anio = cal.get(Calendar.YEAR)
                        viewModel.onFechaNacimientoChange("$dia/$mes/$anio")
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    /**
     * Scaffold = estructura base de Material Design.
     * Provee: TopAppBar arriba, área de contenido en el centro,
     * FAB, SnackbarHost, etc.
     * "paddingValues" contiene el espacio que ocupa la TopAppBar
     * para que el contenido no quede detrás de ella.
     */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_personal_data)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        /**
         * Column con verticalScroll = lista scrollable.
         * Esto es CRÍTICO: garantiza que el teclado nunca tape
         * un campo de texto porque el usuario puede hacer scroll
         * para ver el campo mientras escribe.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── NOMBRES ───────────────────────────────────────────────────────
            OutlinedTextField(
                value = viewModel.nombres,
                onValueChange = { viewModel.onNombresChange(it) },
                label = { Text(stringResource(R.string.label_nombres)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                // isError = true hace que el borde del campo se ponga rojo
                isError = viewModel.errorNombres != null,
                // supportingText = texto pequeño debajo del campo
                supportingText = {
                    viewModel.errorNombres?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    // Capitalización: primera letra de cada palabra en mayúscula
                    capitalization = KeyboardCapitalization.Words,
                    // autoCorrect = false: sin sugerencias de autocompletado
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    // ImeAction.Next: muestra "Siguiente" en el teclado en vez de "Enter"
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    // Al presionar "Siguiente" en el teclado, el foco salta a Apellidos
                    onNext = { focusApellidos.requestFocus() }
                )
            )

            // ── APELLIDOS ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = viewModel.apellidos,
                onValueChange = { viewModel.onApellidosChange(it) },
                label = { Text(stringResource(R.string.label_apellidos)) },
                modifier = Modifier
                    .fillMaxWidth()
                    // focusRequester: registra este campo para recibir foco desde Nombres
                    .focusRequester(focusApellidos),
                singleLine = true,
                isError = viewModel.errorApellidos != null,
                supportingText = {
                    viewModel.errorApellidos?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    // Done = último campo de texto, muestra "Listo" en el teclado
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    // Al presionar "Listo", cierra el teclado
                    onDone = { focusManager.clearFocus() }
                )
            )

            // ── SEXO (RadioButtons) ───────────────────────────────────────────
            Text(
                text = stringResource(R.string.label_sexo),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                /**
                 * forEach en Compose es igual que un bucle for.
                 * Crea un RadioButton por cada opción de la lista.
                 * Mucho más compacto que escribir 3 RadioButtons a mano.
                 */
                sexoOptions.forEach { opcion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            // El RadioButton está seleccionado si su valor
                            // coincide con lo guardado en el ViewModel
                            selected = viewModel.sexo == opcion,
                            onClick = { viewModel.onSexoChange(opcion) }
                        )
                        Text(opcion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // ── FECHA DE NACIMIENTO ───────────────────────────────────────────
            // Campo de solo lectura que muestra la fecha seleccionada
            OutlinedTextField(
                value = viewModel.fechaNacimiento.ifBlank {
                    stringResource(R.string.select_date)
                },
                onValueChange = {},   // readOnly: el usuario no escribe aquí
                readOnly = true,
                label = { Text(stringResource(R.string.label_fecha_nacimiento)) },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.errorFecha != null,
                supportingText = {
                    viewModel.errorFecha?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            // Botón para abrir el diálogo del DatePicker
            TextButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("📅 ${stringResource(R.string.select_date)}")
            }

            // ── GRADO DE ESCOLARIDAD (Spinner) ────────────────────────────────
            /**
             * En XML de Android existe el Spinner.
             * En Jetpack Compose se reemplaza con ExposedDropdownMenuBox:
             * un campo de texto + un menú desplegable debajo.
             */
            ExposedDropdownMenuBox(
                expanded = expandedSpinner,
                onExpandedChange = { expandedSpinner = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.gradoEscolaridad,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_grado_escolaridad)) },
                    // TrailingIcon = la flechita ▼ que indica que es un dropdown
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpinner)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        // menuAnchor() le dice al menú dónde aparecer (debajo de este campo)
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedSpinner,
                    onDismissRequest = { expandedSpinner = false }
                ) {
                    gradosEscolaridad.forEach { grado ->
                        DropdownMenuItem(
                            text = { Text(grado) },
                            onClick = {
                                viewModel.onGradoEscolaridadChange(grado)
                                expandedSpinner = false  // Cierra el menú al seleccionar
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── BOTÓN SIGUIENTE ───────────────────────────────────────────────
            Button(
                onClick = {
                    // Primero valida. Si validar() devuelve true, continúa.
                    if (viewModel.validar()) {
                        viewModel.logData()   // Imprime en Logcat
                        onSiguiente()         // Abre ContactDataActivity
                    }
                    // Si validar() devuelve false, los errores aparecen
                    // automáticamente en los campos porque son mutableStateOf
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_siguiente))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}