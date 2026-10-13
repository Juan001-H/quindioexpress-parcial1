# QuindíoExpress — Parcial 1 (Estructuras de Datos)

Sistema por consola, en Java, para gestionar las operaciones de un centro de distribución de paquetes en el Quindío: registro y consulta de paquetes, municipios destino, despacho por orden de llegada o por prioridad, asignación a repartidores e historial de entregas.

El proyecto usa el Java Collections Framework donde cada necesidad lo justifica y una **lista simplemente enlazada propia** (genérica, con `Iterable`/`Iterator`) para el historial de entregas. Además incluye algoritmos **recursivos**, **búsqueda binaria** y **divide y vencerás**.

## Integrantes

- Tomas Suaza
- Juan Herrera

## Requisitos

- JDK 8 o superior (probado con OpenJDK 23).
- No necesita librerías externas, archivos ni base de datos: los datos iniciales están en `Main.java`.

## Compilar y ejecutar

Desde la raíz del repositorio.

**macOS / Linux**

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out Main
```

**Windows (PowerShell)**

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

> El parámetro `-encoding UTF-8` es importante: sin él, las tildes (Calarcá, Martínez) pueden verse mal en Windows.

## Estructura del proyecto

```
src/
├── Main.java                         Menú interactivo, validación de entradas y datos iniciales
├── modelo/
│   ├── Paquete.java                  Paquete (Comparable: orden natural por código)
│   ├── Repartidor.java               Repartidor (identificación, nombre, zona, disponible)
│   ├── ComparadoresPaquete.java      Comparator: despacho, prioridad, peso y tiempo
│   └── Texto.java                    Normaliza nombres de municipios (mayúsculas y tildes)
├── estructuras/
│   ├── ColeccionSimple.java          Interfaz genérica (extiende Iterable<T>)
│   └── ListaSimplementeEnlazada.java Lista propia con Nodo interno e Iterator
└── servicio/
    └── CentroDistribucion.java       Colecciones y operaciones del sistema
docs/
├── Parcial1_QuindioExpress_PartesA_C.pdf   Parte A (diseño) y Parte C (pruebas y complejidad)
└── Guia_Codigo_QuindioExpress.pdf          Explicación de todo el código, clase por clase
```

## Menú del programa

El menú está organizado en cuatro grupos.

**Gestión operativa**

| Opción | Qué hace | Estructura / algoritmo |
|---|---|---|
| 1 | Registrar un paquete nuevo (valida cada campo y que el código no exista) | `HashMap`, `ArrayList`, `HashSet`, `TreeSet`, `ArrayDeque`, `PriorityQueue` |
| 2 | Buscar un paquete por código | `HashMap` — O(1) |
| 3 | Ver las zonas (municipios) con paquetes, sin repetir y en orden alfabético | `HashSet` y `TreeSet` |
| 4 | Ver los paquetes de un municipio | `HashMap<String, List<Paquete>>` |
| 15 | Ver todos los paquetes pendientes en bodega, en orden de llegada | `ArrayDeque` → copia en `ArrayList` |
| 16 | Eliminar un paquete pendiente registrado por error (se quita de todas las estructuras) | `HashMap`, colas, listas y `Set` |

**Despachos y entregas**

| Opción | Qué hace | Estructura / algoritmo |
|---|---|---|
| 5 | Despachar el siguiente paquete por orden de llegada | `Queue` (`ArrayDeque`) — FIFO |
| 6 | Despachar el paquete de mayor prioridad | `PriorityQueue` + `Comparator` |
| 8 | Ver, registrar o cambiar la disponibilidad de repartidores | `LinkedHashMap` |
| 14 | Registrar la entrega de un repartidor (el paquete pasa al historial y el repartidor queda libre) | `LinkedHashMap` + lista propia |
| 9 | Ver el historial de entregas en el orden real | `ListaSimplementeEnlazada` propia + `Iterator` |

**Análisis algorítmico**

| Opción | Qué hace | Estructura / algoritmo |
|---|---|---|
| 10 | Peso total de los paquetes de un municipio | Recursividad |
| 11 | Cantidad de paquetes con prioridad mayor o igual a un valor | Recursividad |
| 12 | Buscar un paquete por código en la lista ordenada | Búsqueda binaria recursiva |
| 13 | Paquete de mayor peso | Divide y vencerás |

**Reportes y pruebas**

| Opción | Qué hace | Estructura / algoritmo |
|---|---|---|
| 7 | Reportes ordenados por llegada, código, prioridad, peso o tiempo | `Comparable` y `Comparator` |
| 17 | Demostración de la lista propia: los 6 casos de eliminación (vacía, único, primero, intermedio, último e inexistente) y obtener un elemento por posición | `ListaSimplementeEnlazada<String>` |
| 0 | Salir | |

En cualquier campo de una operación se puede escribir **`x`** para cancelarla y volver al menú.

## Flujo de un paquete

```
Registrado ──► Pendiente ──(opción 5 o 6)──► En ruta con un repartidor ──(opción 14)──► Historial de entregas
                (colas)                       repartidor = no disponible               repartidor = disponible
```

Para despachar un paquete debe existir un repartidor **disponible** cuya **zona sea igual al destino** del paquete. Si no lo hay, el paquete se queda en la cola hasta que se registre una entrega (opción 14) o se libere un repartidor (opción 8).

## Reglas validadas

- No se repiten códigos de paquete ni identificaciones de repartidor (sin importar mayúsculas).
- El código debe combinar letras y números (ej. `PQ731`); la identificación del repartidor, letras, números o guiones (ej. `REP-101`).
- Peso mayor que cero (acepta `4.5` o `4,5`).
- Prioridad entera entre 1 y 5.
- Tiempo estimado entero mayor que cero.
- Destino, nombre y zona no pueden estar vacíos ni contener números o símbolos.
- `Calarca`, `calarcá` y `CALARCÁ` se reconocen como el mismo municipio, así que no se crean municipios duplicados.
- Un repartidor no disponible no recibe nuevas asignaciones, y uno con un paquete en ruta no se puede liberar sin registrar la entrega.
- Solo se pueden eliminar paquetes **pendientes**: uno que ya salió en ruta o que ya se entregó no se borra, para no alterar el historial. Si se borra el último paquete de un municipio, el municipio deja de aparecer en la opción 3.
- Si un dato es inválido, el programa explica el error y vuelve a preguntar **solo ese campo**, sin perder lo que ya se había escrito.
- Si la entrada se cierra (Ctrl+D / Ctrl+Z), el programa termina de forma ordenada.

## Datos iniciales

- **30 paquetes** con códigos no consecutivos, prioridades del 1 al 5 y pesos distintos.
- **6 municipios**: Armenia (7 paquetes), Salento (6), Calarcá (5), Circasia (4), Filandia (4) y Montenegro (4).
- **7 repartidores**, todos disponibles al inicio (Armenia tiene dos).

## Decisiones de diseño (resumen)

| Necesidad | Estructura | Por qué |
|---|---|---|
| Orden de registro (auditoría) | `ArrayList` | Conserva el orden y permite `get(i)` en O(1), que usan los algoritmos recursivos |
| Consulta por código | `HashMap<String, Paquete>` | Búsqueda en O(1) promedio, en lugar de recorrer todos los paquetes |
| Municipios sin repetir | `HashSet` | Descarta duplicados en O(1) |
| Municipios en orden alfabético | `TreeSet` | Se mantiene ordenado siempre, O(log m) por inserción |
| Paquetes por municipio | `HashMap<String, List<Paquete>>` | Agrupa por destino conservando el orden de llegada |
| Atención por orden de llegada | `ArrayDeque` como `Queue` | FIFO con `offer`/`poll` en O(1) |
| Despacho prioritario | `PriorityQueue` | El más urgente siempre queda al frente; `poll` en O(log n) |
| Historial de entregas | `ListaSimplementeEnlazada` propia | La exige el enunciado; `agregar` en O(1) gracias a la referencia `cola` |

El análisis completo (tablas, pilas de llamadas, pruebas de escritorio y complejidad) está en [`docs/Parcial1_QuindioExpress_PartesA_C.pdf`](docs/Parcial1_QuindioExpress_PartesA_C.pdf), y la explicación de todo el código en [`docs/Guia_Codigo_QuindioExpress.pdf`](docs/Guia_Codigo_QuindioExpress.pdf).
