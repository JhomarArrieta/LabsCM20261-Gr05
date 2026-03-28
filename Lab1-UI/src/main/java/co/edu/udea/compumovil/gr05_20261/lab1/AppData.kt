package co.edu.udea.compumovil.gr05_20261.lab1

/**
 * "object" en Kotlin = Singleton.
 * Es como una clase pero solo existe UNA instancia en toda la app.
 * Perfecto para datos constantes que varios archivos necesitan leer.
 */
object AppData {

    // Lista de países de Latinoamérica para el autocompletado del campo País
    val paisesLatinoamerica = listOf(
        "Argentina", "Bolivia", "Brasil", "Chile", "Colombia",
        "Costa Rica", "Cuba", "Ecuador", "El Salvador", "Guatemala",
        "Honduras", "México", "Nicaragua", "Panamá", "Paraguay",
        "Perú", "Puerto Rico", "República Dominicana", "Uruguay", "Venezuela"
    )

    // Lista de ciudades de Colombia para el autocompletado del campo Ciudad
    val ciudadesColombia = listOf(
        "Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena",
        "Cúcuta", "Bucaramanga", "Pereira", "Santa Marta", "Ibagué",
        "Pasto", "Manizales", "Neiva", "Armenia", "Villavicencio",
        "Montería", "Valledupar", "Soledad", "Bello", "Buenaventura"
    )
}