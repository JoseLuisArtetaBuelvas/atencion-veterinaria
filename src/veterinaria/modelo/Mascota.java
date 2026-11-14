package veterinaria.modelo;

import java.util.Comparator;
import java.util.Objects;

/**
 * Mascota (paciente) de la clinica.
 *
 * Identificador: codigo (ej. M-001). equals y hashCode usan solo el codigo.
 * Orden natural (Comparable): por codigo. Se usa en el TreeSet de mascotas de cada propietario.
 */
public class Mascota implements Comparable<Mascota> {

    /** Criterio alternativo: especie y luego nombre (y codigo para desempatar). */
    public static final Comparator<Mascota> POR_ESPECIE_Y_NOMBRE =
            Comparator.comparing(Mascota::getEspecie, String.CASE_INSENSITIVE_ORDER)
                      .thenComparing(Mascota::getNombre, String.CASE_INSENSITIVE_ORDER)
                      .thenComparing(Mascota::getCodigo);

    private String codigo;
    private String nombre;
    private String especie;
    private String raza;
    private int edadAnios;
    private Propietario propietario;

    public Mascota(String codigo, String nombre, String especie, String raza, int edadAnios, Propietario propietario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.edadAnios = edadAnios;
        this.propietario = propietario;
    }

    public String getCodigo() {
        return codigo;
    }

    /**
     * Solo la clinica cambia el codigo, porque debe actualizar el Map y el TreeSet donde
     * la mascota esta guardada (ver ClinicaVeterinaria.corregirCodigoMascota).
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public int getEdadAnios() {
        return edadAnios;
    }

    public void setEdadAnios(int edadAnios) {
        this.edadAnios = edadAnios;
    }

    public Propietario getPropietario() {
        return propietario;
    }

    public void setPropietario(Propietario propietario) {
        this.propietario = propietario;
    }

    /** Orden natural por codigo: M-001 antes que M-002. */
    @Override
    public int compareTo(Mascota otra) {
        return codigo.compareTo(otra.codigo);
    }

    /** Dos mascotas son la misma si tienen el mismo codigo. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mascota)) {
            return false;
        }
        return codigo.equals(((Mascota) o).codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo + " " + nombre + " (" + especie + ", " + raza + ", " + edadAnios + " anios)"
                + " - propietario: " + propietario.getNombre();
    }
}
