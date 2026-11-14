package veterinaria.servicio;

import veterinaria.modelo.Atencion;
import veterinaria.modelo.EstadoAtencion;
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
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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

    // 4. Datos ordenados: propietarios por documento y, para cada uno, sus mascotas por codigo
    private final SortedMap<String, SortedSet<Mascota>> mascotasPorPropietario = new TreeMap<>();

    // 5. Atencion por prioridad clinica
    private final PriorityQueue<Atencion> salaDeEspera = new PriorityQueue<>(Atencion.POR_PRIORIDAD_CLINICA);

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
        mascotasPorPropietario.put(doc, new TreeSet<>());   // TreeSet: orden natural de Mascota (codigo)
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
        mascotasPorPropietario.get(propietario.getDocumento()).add(m);
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
        salaDeEspera.add(a);
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

    /** Consulta quien sigue sin sacarlo de la cola (peek). */
    public Atencion verSiguientePaciente() throws VeterinariaException {
        Atencion a = salaDeEspera.peek();
        if (a == null) {
            throw new VeterinariaException("No hay pacientes en la sala de espera");
        }
        return a;
    }

    /** Atiende al paciente de mayor prioridad clinica (poll). */
    public Atencion atenderSiguientePaciente() throws VeterinariaException {
        Atencion a = salaDeEspera.poll();
        if (a == null) {
            throw new VeterinariaException("No hay pacientes en la sala de espera");
        }
        a.setEstado(EstadoAtencion.ATENDIDA);
        mascotasEnEspera.remove(a.getMascota().getCodigo());
        return a;
    }

    /** Lista de la sala de espera en el orden en que seran atendidos (sin vaciar la cola real). */
    public List<Atencion> salaDeEsperaOrdenada() {
        PriorityQueue<Atencion> copia = new PriorityQueue<>(salaDeEspera);
        List<Atencion> orden = new ArrayList<>();
        while (!copia.isEmpty()) {
            orden.add(copia.poll());
        }
        return orden;
    }

    // ==================================================================
    // Modificaciones
    // ==================================================================

    /**
     * Cambia la urgencia de una solicitud en espera. La PriorityQueue no se reordena sola si
     * un elemento cambia por dentro: hay que sacarlo, modificarlo y volver a insertarlo.
     */
    public Atencion cambiarUrgencia(int numero, String urgenciaTexto) throws VeterinariaException {
        Atencion a = buscarAtencion(numero);
        exigirEnEspera(a, "cambiar la urgencia");
        NivelUrgencia nueva = convertirUrgencia(urgenciaTexto);
        salaDeEspera.remove(a);
        a.setUrgencia(nueva);
        salaDeEspera.add(a);
        return a;
    }

    public Atencion cambiarServicio(int numero, String tipoServicio) throws VeterinariaException {
        Atencion a = buscarAtencion(numero);
        exigirEnEspera(a, "cambiar el servicio");
        String servicio = normalizarServicio(obligatorio(tipoServicio, "El tipo de servicio"));
        if (!tiposServicio.contains(servicio)) {
            throw new VeterinariaException("El servicio '" + servicio + "' no esta en el catalogo");
        }
        a.setTipoServicio(servicio);
        return a;
    }

    public Mascota actualizarMascota(String codigo, String nombre, Integer edad) throws VeterinariaException {
        Mascota m = buscarMascota(codigo);
        if (nombre != null && !nombre.isBlank()) {
            m.setNombre(nombre.trim());
        }
        if (edad != null) {
            validarEdad(edad);
            m.setEdadAnios(edad);
        }
        return m;
    }

    /** Pasa una mascota a otro propietario: se mueve de un TreeSet a otro dentro del TreeMap. */
    public Mascota cambiarPropietario(String codigoMascota, String documentoNuevo) throws VeterinariaException {
        Mascota m = buscarMascota(codigoMascota);
        Propietario nuevo = buscarPropietario(documentoNuevo);
        mascotasPorPropietario.get(m.getPropietario().getDocumento()).remove(m);
        m.setPropietario(nuevo);
        mascotasPorPropietario.get(nuevo.getDocumento()).add(m);
        return m;
    }

    /**
     * Corrige el codigo de una mascota. El codigo es la clave del Map, el criterio del TreeSet
     * y el dato del Set de mascotas en espera, asi que las tres colecciones se actualizan:
     * se retira con el codigo viejo, se cambia el codigo y se vuelve a guardar con el nuevo.
     */
    public Mascota corregirCodigoMascota(String codigoActual, String codigoNuevo) throws VeterinariaException {
        Mascota m = buscarMascota(codigoActual);
        String nuevo = validarCodigoMascotaNuevo(codigoNuevo);
        String viejo = m.getCodigo();
        SortedSet<Mascota> delPropietario = mascotasPorPropietario.get(m.getPropietario().getDocumento());

        mascotasPorCodigo.remove(viejo);
        delPropietario.remove(m);                 // se retira ANTES de cambiar el codigo que usa compareTo
        boolean estabaEnEspera = mascotasEnEspera.remove(viejo);

        m.setCodigo(nuevo);

        mascotasPorCodigo.put(nuevo, m);
        delPropietario.add(m);
        if (estabaEnEspera) {
            mascotasEnEspera.add(nuevo);
        }
        return m;
    }

    // ==================================================================
    // Cancelacion y eliminacion
    // ==================================================================

    /** Cancela una solicitud en espera: sale de la sala, queda en el registro como CANCELADA. */
    public Atencion cancelarAtencion(int numero) throws VeterinariaException {
        Atencion a = buscarAtencion(numero);
        exigirEnEspera(a, "cancelarla");
        salaDeEspera.remove(a);
        mascotasEnEspera.remove(a.getMascota().getCodigo());
        a.setEstado(EstadoAtencion.CANCELADA);
        return a;
    }

    /** Elimina una solicitud registrada por error: desaparece de TODAS las colecciones. */
    public Atencion eliminarAtencion(int numero) throws VeterinariaException {
        Atencion a = buscarAtencion(numero);
        registroAtenciones.remove(a);
        atencionesPorNumero.remove(numero);
        if (salaDeEspera.remove(a)) {
            mascotasEnEspera.remove(a.getMascota().getCodigo());
        }
        return a;
    }

    /**
     * Elimina una mascota y, en cascada, sus solicitudes: no puede quedar una atencion
     * apuntando a una mascota que ya no existe en el sistema.
     */
    public List<Atencion> eliminarMascota(String codigo) throws VeterinariaException {
        Mascota m = buscarMascota(codigo);
        List<Atencion> eliminadas = new ArrayList<>();
        for (Atencion a : new ArrayList<>(registroAtenciones)) {
            if (a.getMascota().equals(m)) {
                eliminarAtencion(a.getNumero());
                eliminadas.add(a);
            }
        }
        mascotasPorCodigo.remove(m.getCodigo());
        mascotasPorPropietario.get(m.getPropietario().getDocumento()).remove(m);
        mascotasEnEspera.remove(m.getCodigo());
        return eliminadas;
    }

    // ==================================================================
    // Consultas
    // ==================================================================

    public List<Atencion> getRegistroAtenciones() {
        return Collections.unmodifiableList(registroAtenciones);
    }

    /** Propietarios ordenados por documento, cada uno con sus mascotas ordenadas por codigo. */
    public SortedMap<String, SortedSet<Mascota>> getMascotasPorPropietario() {
        return Collections.unmodifiableSortedMap(mascotasPorPropietario);
    }

    /** Todas las mascotas en un TreeSet con un criterio distinto al natural: especie y nombre. */
    public SortedSet<Mascota> mascotasPorEspecieYNombre() {
        SortedSet<Mascota> ordenadas = new TreeSet<>(Mascota.POR_ESPECIE_Y_NOMBRE);
        ordenadas.addAll(mascotasPorCodigo.values());
        return ordenadas;
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

    public int totalEnEspera() {
        return salaDeEspera.size();
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

    private void exigirEnEspera(Atencion a, String accion) throws VeterinariaException {
        if (a.getEstado() != EstadoAtencion.EN_ESPERA) {
            throw new VeterinariaException("La solicitud #" + a.getNumero() + " esta " + a.getEstado()
                    + ": no se puede " + accion);
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
