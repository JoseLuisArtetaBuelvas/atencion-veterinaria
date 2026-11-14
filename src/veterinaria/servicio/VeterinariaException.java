package veterinaria.servicio;

/** Error de validacion o de regla de negocio de la clinica. El menu muestra su mensaje. */
public class VeterinariaException extends Exception {

    public VeterinariaException(String mensaje) {
        super(mensaje);
    }
}
