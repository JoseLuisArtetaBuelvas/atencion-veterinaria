package veterinaria.modelo;

import java.util.Objects;

/**
 * Solicitud de atencion de una mascota.
 *
 * Identificador: numero (asignado por la clinica, 1001, 1002, ...).
 * ordenLlegada registra en que orden se registro: sirve para desempatar
 * dos solicitudes con la misma urgencia (la que llego primero va primero).
 */
public class Atencion {

    private final int numero;
    private final Mascota mascota;
    private String tipoServicio;
    private String motivo;
    private NivelUrgencia urgencia;
    private EstadoAtencion estado;
    private final long ordenLlegada;

    public Atencion(int numero, Mascota mascota, String tipoServicio, String motivo,
                    NivelUrgencia urgencia, long ordenLlegada) {
        this.numero = numero;
        this.mascota = mascota;
        this.tipoServicio = tipoServicio;
        this.motivo = motivo;
        this.urgencia = urgencia;
        this.ordenLlegada = ordenLlegada;
        this.estado = EstadoAtencion.EN_ESPERA;
    }

    public int getNumero() {
        return numero;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public NivelUrgencia getUrgencia() {
        return urgencia;
    }

    /**
     * Solo la clinica cambia la urgencia, porque debe sacar la atencion de la
     * PriorityQueue y volver a insertarla (ver ClinicaVeterinaria.cambiarUrgencia).
     */
    public void setUrgencia(NivelUrgencia urgencia) {
        this.urgencia = urgencia;
    }

    public EstadoAtencion getEstado() {
        return estado;
    }

    public void setEstado(EstadoAtencion estado) {
        this.estado = estado;
    }

    public long getOrdenLlegada() {
        return ordenLlegada;
    }

    /** Dos atenciones son la misma si tienen el mismo numero. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Atencion)) {
            return false;
        }
        return numero == ((Atencion) o).numero;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }

    @Override
    public String toString() {
        return "#" + numero + " [" + urgencia + "] " + mascota.getNombre() + " (" + mascota.getCodigo() + ")"
                + " - " + tipoServicio + ": " + motivo + " | " + estado;
    }
}
