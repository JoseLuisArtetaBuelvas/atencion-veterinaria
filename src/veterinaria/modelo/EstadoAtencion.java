package veterinaria.modelo;

/** Estados por los que pasa una solicitud de atencion. */
public enum EstadoAtencion {
    /** Registrada y esperando en la cola por urgencia. */
    EN_ESPERA,
    /** Ya fue atendida por el veterinario. */
    ATENDIDA,
    /** Se cancelo antes de ser atendida. */
    CANCELADA
}
