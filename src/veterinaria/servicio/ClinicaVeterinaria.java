package veterinaria.servicio;

import veterinaria.modelo.Atencion;
import veterinaria.modelo.Mascota;
import veterinaria.modelo.NivelUrgencia;
import veterinaria.modelo.Propietario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Logica de la clinica veterinaria: administra todas las colecciones y mantiene su consistencia.
 *
 * Un mismo objeto participa en varias colecciones; cada una ofrece una forma distinta de
 * acceder a el. Por eso toda operacion que agrega, modifica o elimina actualiza TODAS las
 * colecciones afectadas.
 */
public class ClinicaVeterinaria {

    public static final int EDAD_MAXIMA = 30;

    // 1. Coleccion principal: registro general de solicitudes en orden de registro
    private final List<Atencion> registroAtenciones = new ArrayList<>();

    // 2. Busqueda por clave
    private final Map<String, Propietario> propietariosPorDocumento = new HashMap<>();
    private final Map<String, Mascota> mascotasPorCodigo = new HashMap<>();
    private final Map<Integer, Atencion> atencionesPorNumero = new HashMap<>();

    // 3. Datos unicos
    private final Set<String> tiposServicio = new LinkedHashSet<>();   // catalogo sin repetidos, en orden de registro
    private final Set<String> mascotasEnEspera = new HashSet<>();      // una mascota no puede tener dos solicitudes en espera

    private int siguienteNumero = 1001;
    private long contadorLlegadas = 0;

    // ==================================================================
    // Propietarios
    // ==================================================================

    public Propietario registrarPropietario(String documento, String nombre, String telefono) throws VeterinariaException {
        String doc = obligatorio(documento, "El documento del propietario");
        if (!doc.matches("\\d{6,12}")) {
            throw new VeterinariaException("Documento invalido: debe tener entre 6 y 12 digitos");
        }
        if (propietariosPorDocumento.containsKey(doc)) {
            throw new VeterinariaException("Ya existe un propietario con el documento " + doc);
        }
        Propietario p = new Propietario(doc, obligatorio(nombre, "El nombre del propietario"),
                obligatorio(telefono, "El telefono"));
        propietariosPorDocumento.put(doc, p);
        return p;
    }

    public Propietario buscarPropietario(String documento) throws VeterinariaException {
        Propietario p = propietariosPorDocumento.get(limpiar(documento));
        if (p == null) {
            throw new VeterinariaException("No existe un propietario con el documento " + limpiar(documento));
        }
        return p;
    }

    // ==================================================================
    // Mascotas
    // ==================================================================

    public Mascota registrarMascota(String codigo, String nombre, String especie, String raza, int edad,
                                    String documentoPropietario) throws VeterinariaException {
        String cod = validarCodigoMascotaNuevo(codigo);
        validarEdad(edad);
        Propietario propietario = buscarPropietario(documentoPropietario);   // referencia a objeto existente

        Mascota m = new Mascota(cod, obligatorio(nombre, "El nombre de la mascota"),
                obligatorio(especie, "La especie"), obligatorio(raza, "La raza"), edad, propietario);
        mascotasPorCodigo.put(cod, m);
        return m;
    }

    public Mascota buscarMascota(String codigo) throws VeterinariaException {
        String cod = limpiar(codigo).toUpperCase();
        Mascota m = mascotasPorCodigo.get(cod);           // busqueda directa por clave, sin recorrer
        if (m == null) {
            throw new VeterinariaException("No existe una mascota con el codigo " + cod);
        }
        return m;
    }

    public boolean existeMascota(String codigo) {
        return mascotasPorCodigo.containsKey(limpiar(codigo).toUpperCase());
    }

    // ==================================================================
    // Tipos de servicio (Set)
    // ==================================================================

    /** Retorna false si el tipo ya existia: el Set no admite repetidos. */
    public boolean registrarTipoServicio(String tipo) throws VeterinariaException {
        return tiposServicio.add(normalizarServicio(obligatorio(tipo, "El tipo de servicio")));
    }

    public Set<String> getTiposServicio() {
        return Collections.unmodifiableSet(tiposServicio);
    }

    // ==================================================================
    // Solicitudes de atencion
    // ==================================================================

    public Atencion registrarAtencion(String codigoMascota, String tipoServicio, String motivo,
                                      String urgenciaTexto) throws VeterinariaException {
        Mascota mascota = buscarMascota(codigoMascota);
        String servicio = normalizarServicio(obligatorio(tipoServicio, "El tipo de servicio"));
        if (!tiposServicio.contains(servicio)) {
            throw new VeterinariaException("El servicio '" + servicio + "' no esta en el catalogo: " + tiposServicio);
        }
        NivelUrgencia urgencia = convertirUrgencia(urgenciaTexto);
        String descripcion = obligatorio(motivo, "El motivo de la consulta");
        if (mascotasEnEspera.contains(mascota.getCodigo())) {
            throw new VeterinariaException(mascota.getNombre() + " (" + mascota.getCodigo()
                    + ") ya tiene una solicitud en espera");
        }

        Atencion a = new Atencion(siguienteNumero++, mascota, servicio, descripcion, urgencia, ++contadorLlegadas);
        registroAtenciones.add(a);
        atencionesPorNumero.put(a.getNumero(), a);
        mascotasEnEspera.add(mascota.getCodigo());
        return a;
    }

    public Atencion buscarAtencion(int numero) throws VeterinariaException {
        Atencion a = atencionesPorNumero.get(numero);
        if (a == null) {
            throw new VeterinariaException("No existe una solicitud con el numero " + numero);
        }
        return a;
    }

    // ==================================================================
    // Consultas
    // ==================================================================

    public List<Atencion> getRegistroAtenciones() {
        return Collections.unmodifiableList(registroAtenciones);
    }

    public Set<String> getMascotasEnEspera() {
        return Collections.unmodifiableSet(mascotasEnEspera);
    }

    public int totalPropietarios() {
        return propietariosPorDocumento.size();
    }

    public int totalMascotas() {
        return mascotasPorCodigo.size();
    }
    public int totalAtencionesIndexadas() {
        return atencionesPorNumero.size();
    }

    // ==================================================================
    // Validaciones
    // ==================================================================

    private String validarCodigoMascotaNuevo(String codigo) throws VeterinariaException {
        String cod = obligatorio(codigo, "El codigo de la mascota").toUpperCase();
        if (!cod.matches("M-\\d{3}")) {
            throw new VeterinariaException("Codigo de mascota invalido: use el formato M-000 (ej: M-017)");
        }
        if (mascotasPorCodigo.containsKey(cod)) {
            throw new VeterinariaException("Ya existe una mascota con el codigo " + cod);
        }
        return cod;
    }

    private void validarEdad(int edad) throws VeterinariaException {
        if (edad < 0 || edad > EDAD_MAXIMA) {
            throw new VeterinariaException("La edad debe estar entre 0 y " + EDAD_MAXIMA + " anios");
        }
    }

    private NivelUrgencia convertirUrgencia(String texto) throws VeterinariaException {
        try {
            return NivelUrgencia.desdeTexto(texto);
        } catch (IllegalArgumentException e) {
            throw new VeterinariaException(e.getMessage());
        }
    }
    /** "  consulta   GENERAL " -> "Consulta general": evita duplicados que solo difieren en formato. */
    private String normalizarServicio(String tipo) {
        String t = tipo.trim().replaceAll("\\s+", " ").toLowerCase();
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private static String obligatorio(String valor, String campo) throws VeterinariaException {
        if (valor == null || valor.isBlank()) {
            throw new VeterinariaException(campo + " es obligatorio");
        }
        return valor.trim();
    }
}
