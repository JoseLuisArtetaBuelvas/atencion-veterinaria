package veterinaria;

import veterinaria.datos.DatosPrueba;
import veterinaria.servicio.ClinicaVeterinaria;
import veterinaria.servicio.VeterinariaException;
import veterinaria.ui.MenuConsola;

import java.util.Scanner;

/** Punto de entrada: crea la clinica, ofrece cargar los datos de prueba y abre el menu. */
public class App {

    public static void main(String[] args) {
        ClinicaVeterinaria clinica = new ClinicaVeterinaria();
        Scanner entrada = new Scanner(System.in);

        System.out.print("Cargar datos de prueba (10 propietarios, 16 mascotas, 16 solicitudes)? (S/N): ");
        String respuesta = entrada.hasNextLine() ? entrada.nextLine().trim() : "S";
        if (!respuesta.equalsIgnoreCase("N")) {
            try {
                DatosPrueba.cargar(clinica);
                System.out.println("Datos de prueba cargados.");
            } catch (VeterinariaException e) {
                System.out.println("No se pudieron cargar los datos de prueba: " + e.getMessage());
            }
        }
        System.out.println();
        new MenuConsola(clinica, entrada).iniciar();
    }
}
