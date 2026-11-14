package veterinaria.modelo;

/**
 * Nivel de urgencia clinica de una solicitud de atencion.
 *
 * El orden de declaracion define la prioridad: CRITICA se atiende primero y BAJA al final.
 * La PriorityQueue usa ordinal() a traves del Comparator de Atencion.
 */
public enum NivelUrgencia {
    CRITICA("Riesgo vital: trauma, envenenamiento, dificultad respiratoria"),
    ALTA("Dolor fuerte o sintomas que empeoran en horas"),
    MEDIA("Malestar sin riesgo inmediato"),
    BAJA("Control, vacunacion o procedimiento programado");

    private final String descripcion;

    NivelUrgencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Convierte texto del usuario ("critica", "ALTA", ...) en un nivel valido. */
    public static NivelUrgencia desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El nivel de urgencia es obligatorio");
        }
        String limpio = texto.trim().toUpperCase();
        for (NivelUrgencia nivel : values()) {
            if (nivel.name().equals(limpio)) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Urgencia invalida: '" + texto + "'. Use CRITICA, ALTA, MEDIA o BAJA");
    }
}
