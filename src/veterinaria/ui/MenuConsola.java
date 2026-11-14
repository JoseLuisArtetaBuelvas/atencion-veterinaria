package veterinaria.ui;

import veterinaria.modelo.Atencion;
import veterinaria.modelo.Mascota;
import veterinaria.modelo.NivelUrgencia;
import veterinaria.modelo.Propietario;
import veterinaria.servicio.ClinicaVeterinaria;
import veterinaria.servicio.VeterinariaException;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedSet;

/**
 * Menu de consola: solo pide datos y muestra resultados.
 * Toda regla de negocio y todo cambio en las colecciones vive en ClinicaVeterinaria.
 */
public class MenuConsola {

    private final ClinicaVeterinaria clinica;
    private final Scanner entrada;

    public MenuConsola(ClinicaVeterinaria clinica, Scanner entrada) {
        this.clinica = clinica;
        this.entrada = entrada;
    }

    public void iniciar() {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");
            System.out.println();
            try {
                ejecutar(opcion);
            } catch (VeterinariaException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            System.out.println();
        } while (opcion != 0);
    }

    private void mostrarMenu() {
        System.out.println("=============== CLINICA VETERINARIA ===============");
        System.out.println(" REGISTRO");
        System.out.println("  1. Registrar propietario");
        System.out.println("  2. Registrar mascota");
        System.out.println("  3. Registrar solicitud de atencion");
        System.out.println("  4. Tipos de servicio (ver / agregar)");
        System.out.println(" CONSULTAS");
        System.out.println("  5. Ver registro general de solicitudes");
        System.out.println("  6. Buscar mascota por codigo");
        System.out.println("  7. Buscar solicitud por numero");
        System.out.println("  8. Propietarios y sus mascotas (ordenados)");
        System.out.println("  9. Pacientes ordenados por especie y nombre");
        System.out.println(" SALA DE ESPERA");
        System.out.println(" 10. Ver sala de espera en orden de atencion");
        System.out.println(" 11. Ver proximo paciente");
        System.out.println(" 12. Atender siguiente paciente");
        System.out.println(" MODIFICAR / ELIMINAR");
        System.out.println(" 13. Cambiar urgencia de una solicitud");
        System.out.println(" 14. Actualizar datos de una mascota");
        System.out.println(" 15. Cancelar solicitud");
        System.out.println(" 16. Eliminar solicitud registrada por error");
        System.out.println(" 17. Eliminar mascota y sus solicitudes");
        System.out.println(" 18. Estado de las colecciones");
        System.out.println("  0. Salir");
    }

    private void ejecutar(int opcion) throws VeterinariaException {
        switch (opcion) {
            case 1 -> registrarPropietario();
            case 2 -> registrarMascota();
            case 3 -> registrarAtencion();
            case 4 -> tiposDeServicio();
            case 5 -> verRegistroGeneral();
            case 6 -> System.out.println("Encontrada: " + clinica.buscarMascota(leerTexto("Codigo de la mascota: ")));
            case 7 -> System.out.println("Encontrada: " + clinica.buscarAtencion(leerEntero("Numero de solicitud: ")));
            case 8 -> verPropietariosYMascotas();
            case 9 -> verPorEspecieYNombre();
            case 10 -> verSalaDeEspera();
            case 11 -> System.out.println("Proximo paciente: " + clinica.verSiguientePaciente());
            case 12 -> System.out.println("Atendiendo: " + clinica.atenderSiguientePaciente());
            case 13 -> cambiarUrgencia();
            case 14 -> actualizarMascota();
            case 15 -> System.out.println("Cancelada: " + clinica.cancelarAtencion(leerEntero("Numero de solicitud: ")));
            case 16 -> System.out.println("Eliminada de todas las colecciones: "
                    + clinica.eliminarAtencion(leerEntero("Numero de solicitud: ")));
            case 17 -> eliminarMascota();
            case 18 -> estadoColecciones();
            case 0 -> System.out.println("Hasta pronto.");
            default -> System.out.println("Opcion no valida. Elija un numero del 0 al 18.");
        }
    }

    // ------------------------------------------------------------------

    private void registrarPropietario() throws VeterinariaException {
        String doc = leerTexto("Documento: ");
        String nombre = leerTexto("Nombre completo: ");
        String tel = leerTexto("Telefono: ");
        System.out.println("Propietario registrado: " + clinica.registrarPropietario(doc, nombre, tel));
    }

    private void registrarMascota() throws VeterinariaException {
        String codigo = leerTexto("Codigo (M-000): ");
        if (clinica.existeMascota(codigo)) {
            throw new VeterinariaException("Ya existe una mascota con el codigo " + codigo.trim().toUpperCase());
        }
        String nombre = leerTexto("Nombre: ");
        String especie = leerTexto("Especie: ");
        String raza = leerTexto("Raza: ");
        int edad = leerEntero("Edad en anios (0-" + ClinicaVeterinaria.EDAD_MAXIMA + "): ");
        String doc = leerTexto("Documento del propietario: ");
        System.out.println("Mascota registrada: " + clinica.registrarMascota(codigo, nombre, especie, raza, edad, doc));
    }

    private void registrarAtencion() throws VeterinariaException {
        Mascota m = clinica.buscarMascota(leerTexto("Codigo de la mascota: "));
        System.out.println("Paciente: " + m);
        System.out.println("Servicios disponibles: " + clinica.getTiposServicio());
        String servicio = leerTexto("Tipo de servicio: ");
        String motivo = leerTexto("Motivo de la consulta: ");
        mostrarNivelesUrgencia();
        String urgencia = leerTexto("Urgencia: ");
        Atencion a = clinica.registrarAtencion(m.getCodigo(), servicio, motivo, urgencia);
        System.out.println("Solicitud registrada: " + a);
        System.out.println("Guardada en: registro general (List), indice por numero (Map), sala de espera (PriorityQueue)"
                + " y mascotas en espera (Set).");
    }

    private void tiposDeServicio() throws VeterinariaException {
        System.out.println("Catalogo actual: " + clinica.getTiposServicio());
        String nuevo = leerTexto("Nuevo tipo de servicio (Enter para volver): ");
        if (nuevo.isBlank()) {
            return;
        }
        if (clinica.registrarTipoServicio(nuevo)) {
            System.out.println("Servicio agregado. Catalogo: " + clinica.getTiposServicio());
        } else {
            System.out.println("No se agrego: el servicio ya existe en el catalogo (el Set no admite repetidos).");
        }
    }

    private void verRegistroGeneral() {
        List<Atencion> registro = clinica.getRegistroAtenciones();
        System.out.println("REGISTRO GENERAL (" + registro.size() + " solicitudes, en orden de registro):");
        for (Atencion a : registro) {
            System.out.println("  " + a);
        }
    }

    private void verPropietariosYMascotas() {
        System.out.println("PROPIETARIOS ORDENADOS POR DOCUMENTO (TreeMap) Y SUS MASCOTAS POR CODIGO (TreeSet):");
        for (Map.Entry<String, SortedSet<Mascota>> e : clinica.getMascotasPorPropietario().entrySet()) {
            Propietario p;
            try {
                p = clinica.buscarPropietario(e.getKey());
            } catch (VeterinariaException ex) {
                continue;
            }
            System.out.println("  " + e.getKey() + " - " + p.getNombre());
            if (e.getValue().isEmpty()) {
                System.out.println("      (sin mascotas)");
            }
            for (Mascota m : e.getValue()) {
                System.out.println("      " + m.getCodigo() + " " + m.getNombre() + " (" + m.getEspecie() + ")");
            }
        }
    }

    private void verPorEspecieYNombre() {
        System.out.println("PACIENTES POR ESPECIE Y NOMBRE (TreeSet con Comparator):");
        for (Mascota m : clinica.mascotasPorEspecieYNombre()) {
            System.out.printf("  %-8s %-10s %s%n", m.getEspecie(), m.getNombre(), m.getCodigo());
        }
    }

    private void verSalaDeEspera() {
        List<Atencion> orden = clinica.salaDeEsperaOrdenada();
        System.out.println("SALA DE ESPERA (" + orden.size() + " pacientes) en el orden en que seran atendidos:");
        int turno = 1;
        for (Atencion a : orden) {
            System.out.printf("  %2d. %s  (llego en el puesto %d)%n", turno++, a, a.getOrdenLlegada());
        }
    }

    private void cambiarUrgencia() throws VeterinariaException {
        Atencion a = clinica.buscarAtencion(leerEntero("Numero de solicitud: "));
        System.out.println("Actual: " + a);
        mostrarNivelesUrgencia();
        Atencion cambiada = clinica.cambiarUrgencia(a.getNumero(), leerTexto("Nueva urgencia: "));
        System.out.println("Actualizada y reubicada en la sala de espera: " + cambiada);
    }

    private void actualizarMascota() throws VeterinariaException {
        Mascota m = clinica.buscarMascota(leerTexto("Codigo de la mascota: "));
        System.out.println("Actual: " + m);
        System.out.println("  1. Nombre y edad   2. Cambiar propietario   3. Corregir codigo");
        int sub = leerEntero("Que desea actualizar: ");
        switch (sub) {
            case 1 -> {
                String nombre = leerTexto("Nuevo nombre (Enter para conservar): ");
                String edadTxt = leerTexto("Nueva edad (Enter para conservar): ").trim();
                Integer edad = edadTxt.isEmpty() ? null : parsearEntero(edadTxt);
                System.out.println("Actualizada: " + clinica.actualizarMascota(m.getCodigo(), nombre, edad));
            }
            case 2 -> System.out.println("Actualizada: "
                    + clinica.cambiarPropietario(m.getCodigo(), leerTexto("Documento del nuevo propietario: ")));
            case 3 -> System.out.println("Codigo corregido en el Map, el TreeSet y el Set: "
                    + clinica.corregirCodigoMascota(m.getCodigo(), leerTexto("Nuevo codigo (M-000): ")));
            default -> System.out.println("Opcion no valida.");
        }
    }

    private void eliminarMascota() throws VeterinariaException {
        Mascota m = clinica.buscarMascota(leerTexto("Codigo de la mascota: "));
        String ok = leerTexto("Se eliminara " + m.getNombre() + " y todas sus solicitudes. Confirmar (S/N): ");
        if (!ok.trim().equalsIgnoreCase("S")) {
            System.out.println("Operacion cancelada.");
            return;
        }
        List<Atencion> eliminadas = clinica.eliminarMascota(m.getCodigo());
        System.out.println("Mascota eliminada. Solicitudes eliminadas con ella: " + eliminadas.size());
    }

    private void estadoColecciones() {
        System.out.println("ESTADO DE LAS COLECCIONES");
        System.out.println("  ArrayList  registro general de solicitudes : " + clinica.getRegistroAtenciones().size());
        System.out.println("  HashMap    solicitudes por numero          : " + clinica.totalAtencionesIndexadas());
        System.out.println("  HashMap    mascotas por codigo             : " + clinica.totalMascotas());
        System.out.println("  HashMap    propietarios por documento      : " + clinica.totalPropietarios());
        System.out.println("  TreeMap    propietarios con sus mascotas   : " + clinica.getMascotasPorPropietario().size());
        System.out.println("  PriorityQueue sala de espera               : " + clinica.totalEnEspera());
        System.out.println("  HashSet    mascotas en espera              : " + clinica.getMascotasEnEspera());
        System.out.println("  LinkedHashSet tipos de servicio            : " + clinica.getTiposServicio());
        boolean coherente = clinica.getRegistroAtenciones().size() == clinica.totalAtencionesIndexadas()
                && clinica.totalEnEspera() == clinica.getMascotasEnEspera().size();
        System.out.println("  Consistencia (List = Map y Cola = Set)     : " + (coherente ? "OK" : "REVISAR"));
    }

    // ------------------------------------------------------------------

    private void mostrarNivelesUrgencia() {
        for (NivelUrgencia n : NivelUrgencia.values()) {
            System.out.println("    " + n + " - " + n.getDescripcion());
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return entrada.hasNextLine() ? entrada.nextLine() : "";
    }

    private int leerEntero(String mensaje) {
        while (true) {
            String linea = leerTexto(mensaje).trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                if (!entrada.hasNextLine() && linea.isEmpty()) {
                    return 0;   // fin de la entrada: salir
                }
                System.out.println("Debe escribir un numero entero.");
            }
        }
    }

    private int parsearEntero(String texto) throws VeterinariaException {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new VeterinariaException("'" + texto + "' no es un numero entero");
        }
    }
}
