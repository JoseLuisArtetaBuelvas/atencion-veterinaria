package veterinaria.modelo;

import java.util.Objects;

/**
 * Propietario (tutor) de una o varias mascotas.
 * Identificador: documento. equals y hashCode usan solo ese atributo.
 */
public class Propietario {

    private final String documento;
    private String nombre;
    private String telefono;

    public Propietario(String documento, String nombre, String telefono) {
        this.documento = documento;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /** Dos propietarios son el mismo si tienen el mismo documento. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Propietario)) {
            return false;
        }
        return documento.equals(((Propietario) o).documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }

    @Override
    public String toString() {
        return nombre + " (doc. " + documento + ", tel. " + telefono + ")";
    }
}
