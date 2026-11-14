package veterinaria.datos;

import veterinaria.servicio.ClinicaVeterinaria;
import veterinaria.servicio.VeterinariaException;

/**
 * Datos de prueba preparados para demostrar el comportamiento de las colecciones.
 *
 * - 10 propietarios: algunos con varias mascotas (relacion uno a muchos en el TreeMap).
 * - 16 mascotas: dos se llaman "Luna" (perro y gato) y hay varias especies, para el orden por especie y nombre.
 * - 6 tipos de servicio en el catalogo (Set).
 * - 16 solicitudes registradas en un orden que NO coincide con su urgencia: la primera es BAJA
 *   y la primera CRITICA llega en el quinto lugar.
 * - Los documentos y codigos no son consecutivos en el orden de registro, para que se note el TreeMap.
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    public static void cargar(ClinicaVeterinaria c) throws VeterinariaException {
        String[] servicios = {"Consulta general", "Vacunacion", "Urgencias", "Cirugia", "Desparasitacion", "Odontologia"};
        for (String s : servicios) {
            c.registrarTipoServicio(s);
        }

        c.registrarPropietario("1047382910", "Laura Martinez", "3004567812");
        c.registrarPropietario("73145226", "Carlos Rodriguez", "3157894561");
        c.registrarPropietario("1143789456", "Ana Gomez", "3012345678");
        c.registrarPropietario("45698712", "Jorge Herrera", "3209876543");
        c.registrarPropietario("1001234567", "Valentina Rios", "3112223344");
        c.registrarPropietario("92541873", "Pedro Castillo", "3165554433");
        c.registrarPropietario("1052987654", "Sofia Diaz", "3046667788");
        c.registrarPropietario("88123456", "Andres Peralta", "3178889900");
        c.registrarPropietario("1129876543", "Camila Torres", "3023334455");
        c.registrarPropietario("64789123", "Luis Fernando Vega", "3141112233");

        c.registrarMascota("M-007", "Max", "Perro", "Labrador", 5, "1047382910");
        c.registrarMascota("M-002", "Luna", "Gato", "Siames", 3, "1047382910");
        c.registrarMascota("M-013", "Rocky", "Perro", "Bulldog", 7, "73145226");
        c.registrarMascota("M-001", "Kiara", "Perro", "Golden retriever", 2, "1143789456");
        c.registrarMascota("M-010", "Michi", "Gato", "Persa", 9, "45698712");
        c.registrarMascota("M-004", "Toby", "Perro", "Beagle", 4, "1001234567");
        c.registrarMascota("M-015", "Pelusa", "Conejo", "Cabeza de leon", 1, "1001234567");
        c.registrarMascota("M-003", "Simba", "Gato", "Mestizo", 6, "92541873");
        c.registrarMascota("M-011", "Coco", "Ave", "Loro amazonico", 12, "1052987654");
        c.registrarMascota("M-006", "Luna", "Perro", "Pastor aleman", 8, "88123456");
        c.registrarMascota("M-012", "Nala", "Gato", "Angora", 2, "1129876543");
        c.registrarMascota("M-005", "Bruno", "Perro", "Rottweiler", 10, "64789123");
        c.registrarMascota("M-014", "Manchas", "Perro", "Dalmata", 6, "73145226");
        c.registrarMascota("M-008", "Kiwi", "Ave", "Periquito", 1, "1047382910");
        c.registrarMascota("M-009", "Tom", "Hamster", "Sirio", 1, "1143789456");
        c.registrarMascota("M-016", "Zeus", "Perro", "Husky siberiano", 3, "1052987654");

        // Orden de llegada intencionalmente distinto al orden de prioridad
        c.registrarAtencion("M-007", "Vacunacion", "Refuerzo anual de rabia", "BAJA");
        c.registrarAtencion("M-002", "Consulta general", "Perdida de apetito hace dos dias", "MEDIA");
        c.registrarAtencion("M-013", "Desparasitacion", "Control trimestral", "BAJA");
        c.registrarAtencion("M-001", "Consulta general", "Cojea de la pata trasera", "ALTA");
        c.registrarAtencion("M-010", "Urgencias", "Ingirio veneno para ratas", "CRITICA");
        c.registrarAtencion("M-004", "Odontologia", "Sarro y mal aliento", "BAJA");
        c.registrarAtencion("M-015", "Consulta general", "Diarrea desde ayer", "MEDIA");
        c.registrarAtencion("M-003", "Urgencias", "Atropellado, sangrado abundante", "CRITICA");
        c.registrarAtencion("M-011", "Consulta general", "Plumas arrancadas", "MEDIA");
        c.registrarAtencion("M-006", "Cirugia", "Herida profunda en la pata", "ALTA");
        c.registrarAtencion("M-012", "Vacunacion", "Primera vacuna triple felina", "BAJA");
        c.registrarAtencion("M-005", "Urgencias", "Dificultad para respirar", "CRITICA");
        c.registrarAtencion("M-014", "Consulta general", "Vomito repetido", "ALTA");
        c.registrarAtencion("M-008", "Consulta general", "Pico crecido", "BAJA");
        c.registrarAtencion("M-009", "Consulta general", "No se mueve mucho", "MEDIA");
        c.registrarAtencion("M-016", "Desparasitacion", "Control de cachorro", "BAJA");
    }
}
