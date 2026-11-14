# Sistema de atención veterinaria con Java Collections Framework

Actividad 1 de la Unidad 3 de **Estructuras de Datos** (Universidad de Cartagena):
*Diseño de un sistema de gestión utilizando Java Collections Framework*.
Caso asignado: **3 – Sistema de atención veterinaria**.

**Autor:** José Luis Arteta Buelvas

## Problema

Una clínica veterinaria registra propietarios, sus mascotas y las solicitudes de atención de cada
mascota. Las solicitudes no se atienden por orden de llegada sino por **urgencia clínica**
(CRÍTICA, ALTA, MEDIA, BAJA): un animal envenenado pasa antes que una vacunación aunque haya
llegado después. El sistema evita datos duplicados, busca mascotas y solicitudes por su
identificador y mantiene listados ordenados.

## Versión de Java

JDK 17 (no usa librerías externas ni herramientas de construcción).

## Cómo compilar y ejecutar

En PowerShell, desde la carpeta del proyecto:

```powershell
# Si java no está en el PATH
$env:JAVA_HOME="$env:USERPROFILE\.jdks\ms-17.0.20.1"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

# Compilar
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src, test).FullName

# Ejecutar (al iniciar pregunta si carga los datos de prueba)
java -cp out veterinaria.App

# Casos de prueba
java -cp out veterinaria.PruebasClinica
```

En Linux/macOS: `javac -d out $(find src test -name "*.java")`.

## Colecciones utilizadas

| Necesidad | Interfaz | Implementación |
|---|---|---|
| Registro general de solicitudes en orden de registro | `List<Atencion>` | `ArrayList` |
| Buscar propietario por documento | `Map<String, Propietario>` | `HashMap` |
| Buscar mascota por código | `Map<String, Mascota>` | `HashMap` |
| Buscar solicitud por número | `Map<Integer, Atencion>` | `HashMap` |
| Catálogo de tipos de servicio sin repetidos | `Set<String>` | `LinkedHashSet` |
| Impedir dos solicitudes en espera para la misma mascota | `Set<String>` | `HashSet` |
| Propietarios ordenados por documento con sus mascotas ordenadas por código | `SortedMap<String, SortedSet<Mascota>>` | `TreeMap` + `TreeSet` |
| Pacientes ordenados por especie y nombre | `SortedSet<Mascota>` | `TreeSet` con `Comparator` |
| Sala de espera por urgencia clínica | `Queue<Atencion>` | `PriorityQueue` con `Comparator` |

## Estructura

```
src/veterinaria/
  App.java                     Punto de entrada
  modelo/                      Propietario, Mascota, Atencion, NivelUrgencia, EstadoAtencion
  servicio/                    ClinicaVeterinaria (colecciones y reglas), VeterinariaException
  datos/                       DatosPrueba (10 propietarios, 16 mascotas, 16 solicitudes)
  ui/                          MenuConsola
test/veterinaria/
  PruebasClinica.java          Casos de prueba sin librerías externas
```

## Documentación de la actividad

Guía: *Estructuras de datos – Unidad 3 – Actividad Protocolo Individual* (SIMA-PESAD).
Referencia de las colecciones: [Java Collections Framework – Oracle](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/package-summary.html).
