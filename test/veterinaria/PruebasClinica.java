package veterinaria;

import veterinaria.datos.DatosPrueba;
import veterinaria.modelo.Atencion;
import veterinaria.modelo.EstadoAtencion;
import veterinaria.modelo.Mascota;
import veterinaria.modelo.NivelUrgencia;
import veterinaria.modelo.Propietario;
import veterinaria.servicio.ClinicaVeterinaria;
import veterinaria.servicio.VeterinariaException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Casos de prueba del sistema (sin librerias externas).
 *
 * Ejecutar:  java -cp out veterinaria.PruebasClinica
 */
public class PruebasClinica {

    private static int aprobadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        probarEqualsYHashCode();
        probarRegistroYMap();
        probarSets();
        probarOrden();
        probarPrioridad();
        probarModificaciones();
        probarEliminacion();
        probarValidaciones();

        System.out.println();
        System.out.println("Resultado: " + aprobadas + " aprobadas, " + fallidas + " fallidas.");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static ClinicaVeterinaria clinicaConDatos() throws VeterinariaException {
        ClinicaVeterinaria c = new ClinicaVeterinaria();
        DatosPrueba.cargar(c);
        return c;
    }

    private static void probarEqualsYHashCode() {
        System.out.println("== equals y hashCode ==");
        Propietario p = new Propietario("123456", "A", "1");
        Mascota a = new Mascota("M-001", "Kiara", "Perro", "X", 2, p);
        Mascota b = new Mascota("M-001", "Otro nombre", "Gato", "Y", 9, p);
        verificar("mascotas con el mismo codigo son iguales", a.equals(b), true);
        verificar("y tienen el mismo hashCode", a.hashCode() == b.hashCode(), true);
        Set<Mascota> set = new HashSet<>();
        set.add(a);
        set.add(b);
        verificar("un HashSet las trata como un solo elemento", set.size(), 1);
        verificar("compareTo por codigo", a.compareTo(new Mascota("M-002", "Z", "Z", "Z", 1, p)) < 0, true);
    }

    private static void probarRegistroYMap() throws VeterinariaException {
        System.out.println("== Registro general y busqueda por clave ==");
        ClinicaVeterinaria c = clinicaConDatos();
        verificar("16 solicitudes en el registro general", c.getRegistroAtenciones().size(), 16);
        verificar("16 mascotas en el Map", c.totalMascotas(), 16);
        verificar("10 propietarios en el Map", c.totalPropietarios(), 10);
        verificar("el registro conserva el orden de llegada", c.getRegistroAtenciones().get(0).getNumero(), 1001);
        verificar("buscar mascota por codigo (sin importar mayusculas)", c.buscarMascota("m-010").getNombre(), "Michi");
        verificar("buscar solicitud por numero", c.buscarAtencion(1005).getMascota().getCodigo(), "M-010");
        verificar("containsKey de mascota existente", c.existeMascota("M-016"), true);
        verificarExcepcion("buscar mascota inexistente", () -> c.buscarMascota("M-999"));
        verificarExcepcion("buscar solicitud inexistente", () -> c.buscarAtencion(9999));
        verificarExcepcion("propietario inexistente", () -> c.buscarPropietario("11111111"));
    }

    private static void probarSets() throws VeterinariaException {
        System.out.println("== Datos unicos con Set ==");
        ClinicaVeterinaria c = clinicaConDatos();
        verificar("agregar servicio nuevo", c.registrarTipoServicio("Peluqueria"), true);
        verificar("servicio repetido con otro formato se rechaza", c.registrarTipoServicio("  CONSULTA   general "), false);
        verificar("el catalogo conserva el orden de registro", c.getTiposServicio().iterator().next(), "Consulta general");
        verificarExcepcion("una mascota no puede tener dos solicitudes en espera",
                () -> c.registrarAtencion("M-007", "Consulta general", "Otra cosa", "ALTA"));
        verificar("16 mascotas en espera", c.getMascotasEnEspera().size(), 16);
        verificar("tieneSolicitudEnEspera detecta la espera", c.tieneSolicitudEnEspera("m-007"), true);
    }

    private static void probarOrden() throws VeterinariaException {
        System.out.println("== Orden con TreeMap, TreeSet y Comparator ==");
        ClinicaVeterinaria c = clinicaConDatos();
        verificar("TreeMap: primer documento en orden numerico", c.getMascotasPorPropietario().firstKey(), "45698712");
        verificar("TreeMap: ultimo documento en orden numerico", c.getMascotasPorPropietario().lastKey(), "1143789456");
        List<String> codigosLaura = new ArrayList<>();
        for (Mascota m : c.getMascotasPorPropietario().get("1047382910")) {
            codigosLaura.add(m.getCodigo());
        }
        verificar("TreeSet: mascotas de Laura ordenadas por codigo", codigosLaura.toString(), "[M-002, M-007, M-008]");
        Mascota primera = c.mascotasPorEspecieYNombre().first();
        verificar("Comparator especie+nombre: primero Ave Coco", primera.getNombre(), "Coco");
        int lunas = 0;
        for (Mascota m : c.mascotasPorEspecieYNombre()) {
            if (m.getNombre().equals("Luna")) {
                lunas++;
            }
        }
        verificar("las dos Luna se conservan (el Comparator desempata)", lunas, 2);
    }

    private static void probarPrioridad() throws VeterinariaException {
        System.out.println("== Prioridad clinica con PriorityQueue ==");
        ClinicaVeterinaria c = clinicaConDatos();
        verificar("peek: primero la CRITICA que llego antes (Michi)", c.verSiguientePaciente().getMascota().getNombre(), "Michi");
        verificar("peek no saca de la cola", c.totalEnEspera(), 16);
        Atencion a1 = c.atenderSiguientePaciente();
        Atencion a2 = c.atenderSiguientePaciente();
        Atencion a3 = c.atenderSiguientePaciente();
        verificar("orden de las criticas: Michi, Simba, Bruno",
                a1.getMascota().getNombre() + "," + a2.getMascota().getNombre() + "," + a3.getMascota().getNombre(),
                "Michi,Simba,Bruno");
        verificar("despues de las criticas sigue una ALTA", c.verSiguientePaciente().getUrgencia(), NivelUrgencia.ALTA);
        verificar("atendida queda ATENDIDA", a1.getEstado(), EstadoAtencion.ATENDIDA);
        verificar("atendida sale del Set de espera", c.getMascotasEnEspera().contains("M-010"), false);
        verificar("atendida sigue en el registro general", c.getRegistroAtenciones().contains(a1), true);
        verificar("la mascota atendida puede pedir una nueva cita",
                c.registrarAtencion("M-010", "Consulta general", "Control post envenenamiento", "MEDIA").getNumero(), 1017);
        List<Atencion> orden = c.salaDeEsperaOrdenada();
        verificar("la sala ordenada no vacia la cola real", c.totalEnEspera(), orden.size());
        verificar("la ultima es BAJA", orden.get(orden.size() - 1).getUrgencia(), NivelUrgencia.BAJA);
    }

    private static void probarModificaciones() throws VeterinariaException {
        System.out.println("== Modificaciones y consistencia ==");
        ClinicaVeterinaria c = clinicaConDatos();
        c.cambiarUrgencia(1001, "critica");   // Max: BAJA -> CRITICA, llego primero
        verificar("al subir la urgencia pasa al frente", c.verSiguientePaciente().getNumero(), 1001);
        c.cambiarUrgencia(1001, "BAJA");
        verificar("al bajarla vuelve atras", c.verSiguientePaciente().getMascota().getNombre(), "Michi");
        verificar("la cola mantiene su tamano", c.totalEnEspera(), 16);

        c.cambiarPropietario("M-008", "64789123");
        verificar("Kiwi sale del TreeSet de Laura", c.getMascotasPorPropietario().get("1047382910").size(), 2);
        verificar("y entra al de Luis Fernando", c.getMascotasPorPropietario().get("64789123").size(), 2);

        c.corregirCodigoMascota("M-016", "M-020");
        verificar("codigo viejo ya no existe en el Map", c.existeMascota("M-016"), false);
        verificar("codigo nuevo si existe en el Map", c.buscarMascota("M-020").getNombre(), "Zeus");
        verificar("el Set de espera usa el codigo nuevo",
                c.getMascotasEnEspera().contains("M-020") && !c.getMascotasEnEspera().contains("M-016"), true);
        verificar("el TreeSet del propietario se reordeno",
                c.getMascotasPorPropietario().get("1052987654").last().getCodigo(), "M-020");
        verificar("la solicitud apunta a la mascota con el codigo nuevo",
                c.buscarAtencion(1016).getMascota().getCodigo(), "M-020");
        verificarExcepcion("no se puede corregir a un codigo existente", () -> c.corregirCodigoMascota("M-020", "M-001"));

        c.actualizarMascota("M-004", "Toby Junior", 5);
        verificar("actualizar nombre y edad", c.buscarMascota("M-004").getNombre() + "/" + c.buscarMascota("M-004").getEdadAnios(), "Toby Junior/5");
        verificar("cambiar servicio de una solicitud", c.cambiarServicio(1006, "consulta general").getTipoServicio(), "Consulta general");
    }

    private static void probarEliminacion() throws VeterinariaException {
        System.out.println("== Cancelacion y eliminacion ==");
        ClinicaVeterinaria c = clinicaConDatos();
        Atencion cancelada = c.cancelarAtencion(1003);
        verificar("cancelar: estado CANCELADA", cancelada.getEstado(), EstadoAtencion.CANCELADA);
        verificar("cancelar: sale de la sala de espera", c.totalEnEspera(), 15);
        verificar("cancelar: sale del Set", c.getMascotasEnEspera().contains("M-013"), false);
        verificar("cancelar: sigue en el registro y en el Map", c.buscarAtencion(1003).getNumero(), 1003);
        verificarExcepcion("no se puede cancelar dos veces", () -> c.cancelarAtencion(1003));

        c.eliminarAtencion(1002);
        verificar("eliminar: sale del registro general", c.getRegistroAtenciones().size(), 15);
        verificarExcepcion("eliminar: ya no se encuentra en el Map", () -> c.buscarAtencion(1002));
        verificar("eliminar: sale de la sala y del Set", c.totalEnEspera() == 14 && !c.getMascotasEnEspera().contains("M-002"), true);

        List<Atencion> borradas = c.eliminarMascota("M-005");
        verificar("eliminar mascota borra su solicitud", borradas.size(), 1);
        verificarExcepcion("la mascota ya no se encuentra", () -> c.buscarMascota("M-005"));
        verificarExcepcion("su solicitud tampoco", () -> c.buscarAtencion(1012));
        verificar("ni queda en el TreeSet del propietario", c.getMascotasPorPropietario().get("64789123").isEmpty(), true);
        verificar("colecciones sincronizadas", c.getRegistroAtenciones().size() == c.totalAtencionesIndexadas()
                && c.totalEnEspera() == c.getMascotasEnEspera().size(), true);
    }

    private static void probarValidaciones() throws VeterinariaException {
        System.out.println("== Validaciones ==");
        ClinicaVeterinaria c = clinicaConDatos();
        verificarExcepcion("documento duplicado", () -> c.registrarPropietario("73145226", "X", "1"));
        verificarExcepcion("documento con letras", () -> c.registrarPropietario("ABC123", "X", "1"));
        verificarExcepcion("codigo de mascota duplicado", () -> c.registrarMascota("m-001", "X", "Perro", "X", 1, "73145226"));
        verificarExcepcion("codigo con formato invalido", () -> c.registrarMascota("PERRO1", "X", "Perro", "X", 1, "73145226"));
        verificarExcepcion("edad fuera de rango", () -> c.registrarMascota("M-030", "X", "Perro", "X", 31, "73145226"));
        verificarExcepcion("propietario inexistente", () -> c.registrarMascota("M-030", "X", "Perro", "X", 1, "999999"));
        verificarExcepcion("nombre vacio", () -> c.registrarMascota("M-030", " ", "Perro", "X", 1, "73145226"));
        c.registrarMascota("M-030", "Firulais", "Perro", "Criollo", 2, "73145226");
        verificarExcepcion("mascota inexistente en solicitud", () -> c.registrarAtencion("M-099", "Urgencias", "X", "ALTA"));
        verificarExcepcion("servicio fuera del catalogo", () -> c.registrarAtencion("M-030", "Spa", "X", "ALTA"));
        verificarExcepcion("urgencia invalida", () -> c.registrarAtencion("M-030", "Urgencias", "X", "MUY ALTA"));
        verificarExcepcion("motivo vacio", () -> c.registrarAtencion("M-030", "Urgencias", "", "ALTA"));
        verificar("ningun dato invalido entro", c.getRegistroAtenciones().size() + "/" + c.totalMascotas(), "16/17");
        c.atenderSiguientePaciente();
        verificarExcepcion("no se cambia la urgencia de una atendida", () -> c.cambiarUrgencia(1005, "BAJA"));
    }

    // ------------------------------------------------------------------

    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void verificar(String nombre, Object obtenido, Object esperado) {
        if (obtenido.equals(esperado)) {
            aprobadas++;
            System.out.println("  [OK]    " + nombre);
        } else {
            fallidas++;
            System.out.println("  [FALLA] " + nombre + " -> esperado <" + esperado + ">, obtenido <" + obtenido + ">");
        }
    }

    private static void verificarExcepcion(String nombre, Accion accion) {
        try {
            accion.ejecutar();
            fallidas++;
            System.out.println("  [FALLA] " + nombre + " -> se esperaba una excepcion");
        } catch (Exception e) {
            aprobadas++;
            System.out.println("  [OK]    " + nombre + "  (" + e.getMessage() + ")");
        }
    }
}
