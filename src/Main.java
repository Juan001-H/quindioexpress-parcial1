import estructuras.ColeccionSimple;
import java.util.List;
import java.util.Scanner;
import modelo.Paquete;
import java.util.Map;
import java.util.NoSuchElementException;
import modelo.Repartidor;
import servicio.CentroDistribucion;

public class Main {

    // Se lanza cuando el usuario escribe 'x' en un campo para abandonar la operación actual
    private static class OperacionCancelada extends RuntimeException {
        private static final long serialVersionUID = 1L;

        OperacionCancelada() {
            super("Operación cancelada por el usuario.");
        }
    }

    // Se lanza cuando ya no hay más entrada (Ctrl+D / Ctrl+Z o fin de archivo)
    private static class FinDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public static void main(String[] args) {
        CentroDistribucion centro = new CentroDistribucion();
        cargarDatosBase(centro);

        Scanner sc = new Scanner(System.in);

        try {
            ejecutarMenu(centro, sc);
        } catch (FinDeEntrada e) {
            System.out.println("\n\nNo hay más entrada. Programa finalizado.");
        }
        sc.close();
    }

    private static void ejecutarMenu(CentroDistribucion centro, Scanner sc) {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n================ QUINDÍO EXPRESS (Total: "
                    + centro.getPaquetesOrdenLlegada().size() + " | Pendientes: "
                    + centro.obtenerCantidadPendientes() + ") ================");
            System.out.println(" 1. Registrar paquete nuevo            |  8. Gestionar repartidores (Ver/Registrar/Liberar)");
            System.out.println(" 2. Consultar paquete por código O(1)  |  9. Historial entregas (Lista Propia)");
            System.out.println(" 3. Ver municipios (HashSet y TreeSet) | 10. Recursividad: Peso total por municipio");
            System.out.println(" 4. Ver paquetes de un municipio       | 11. Recursividad: Contar por prioridad");
            System.out.println(" 5. Despachar 1 paquete (Orden llegada)| 12. Búsqueda Binaria por código");
            System.out.println(" 6. Despachar 1 paquete (Por prioridad)| 13. Divide y Vencerás: Paquete más pesado");
            System.out.println(" 7. Reportes ordenados (Comparator)    | 14. Registrar entrega (libera al repartidor)");
            System.out.println(" 0. Salir                              |     En cualquier campo escriba 'x' para cancelar");
            System.out.println("=====================================================================================");

            opcion = leerOpcion(sc, "-> Elija una opción (0-14): ", 0, 14);

            try {
                switch (opcion) {
                    case 1:
                        opcionRegistrarPaquete(centro, sc);
                        break;
                    case 2:
                        opcionConsultarPorCodigo(centro, sc);
                        break;
                    case 3:
                        System.out.println("\n--- 3. MUNICIPIOS CON ENTREGAS ---");
                        System.out.println("* Sin duplicados (HashSet):   " + centro.obtenerMunicipiosConEntregas());
                        System.out.println("* Orden alfabético (TreeSet): " + centro.obtenerMunicipiosOrdenados());
                        break;
                    case 4:
                        opcionPaquetesPorMunicipio(centro, sc);
                        break;
                    case 5:
                        opcionDespacharLlegada(centro);
                        break;
                    case 6:
                        opcionDespacharPrioridad(centro);
                        break;
                    case 7:
                        opcionReportesOrdenados(centro, sc);
                        break;
                    case 8:
                        opcionGestionarRepartidores(centro, sc);
                        break;
                    case 9:
                        opcionHistorialEntregas(centro, sc);
                        break;
                    case 10:
                        opcionRecursivaPesoMunicipio(centro, sc);
                        break;
                    case 11:
                        opcionRecursivaContarPrioridad(centro, sc);
                        break;
                    case 12:
                        opcionBusquedaBinaria(centro, sc);
                        break;
                    case 13:
                        System.out.println("\n--- 13. DIVIDE Y VENCERÁS: PAQUETE DE MAYOR PESO ---");
                        System.out.println("-> Paquete más pesado encontrado: " + centro.obtenerPaqueteMayorPesoDivideYVenceras());
                        break;
                    case 14:
                        opcionRegistrarEntrega(centro, sc);
                        break;
                    case 0:
                        System.out.println("Programa finalizado correctamente.");
                        break;
                }
            } catch (OperacionCancelada e) {
                System.out.println("\n[CANCELADO]: " + e.getMessage());
            } catch (FinDeEntrada e) {
                throw e;
            } catch (IllegalArgumentException e) {
                System.out.println("\n[EXCEPCIÓN DE VALIDACIÓN]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[ERROR INESPERADO]: " + e.getMessage());
            }

            if (opcion != 0) {
                System.out.print("\n[Presione ENTER para volver al menú]");
                leerLinea(sc);
            }
        }
    }

    // =========================================================================
    // FUNCIONALIDADES INTERACTIVAS CON VALIDACIÓN INMEDIATA CAMPO POR CAMPO
    // =========================================================================

    private static void opcionRegistrarPaquete(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 1. REGISTRAR PAQUETE NUEVO ---");

        // 1. Valida formato de código inmediatamente y verifica duplicado antes de seguir
        String codigo = leerCodigoAlfanumerico(sc, "Código del paquete (letras y números, ej. PQ888): ");
        while (centro.consultarPaquetePorCodigo(codigo) != null) {
            error("Ya existe un paquete con el código '" + codigo + "'.");
            codigo = leerCodigoAlfanumerico(sc, "Código del paquete (letras y números, ej. PQ888): ");
        }

        // 2. Valida que el municipio solo tenga letras; si ya existe (ej. "calarca") se usa el nombre registrado ("Calarcá")
        String destino = centro.resolverNombreMunicipio(
                leerTextoSoloLetras(sc, "Municipio destino (solo letras, ej. Armenia, Salento): "));

        // 3. Valida que el peso sea numérico decimal > 0
        double peso = leerDoublePositivo(sc, "Peso en kg (número > 0, ej. 4.5): ");

        // 4. Valida que la prioridad sea entero entre 1 y 5
        int prioridad = leerEnteroEnRango(sc, "Prioridad (entero entre 1 y 5): ", 1, 5);

        // 5. Valida que el tiempo estimado sea entero positivo > 0
        int tiempo = leerEnteroPositivo(sc, "Tiempo estimado en minutos (entero > 0, ej. 30): ");

        Paquete nuevo = new Paquete(codigo, destino, peso, prioridad, tiempo);
        centro.registrarPaquete(nuevo);
        System.out.println("[OK] Paquete registrado exitosamente: " + nuevo);
    }

    private static void opcionConsultarPorCodigo(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 2. CONSULTA RÁPIDA POR CÓDIGO (HashMap O(1)) ---");
        String codigo = leerCodigoAlfanumerico(sc, "Ingrese código a consultar (ej. PQ512, PQ731): ");
        Paquete encontrado = centro.consultarPaquetePorCodigo(codigo);
        if (encontrado != null) {
            System.out.println("-> Paquete encontrado: " + encontrado);
        } else {
            System.out.println("-> No existe ningún paquete registrado con el código '" + codigo + "'.");
        }
    }

    private static void opcionPaquetesPorMunicipio(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 4. PAQUETES POR MUNICIPIO ---");
        System.out.println("Municipios disponibles: " + centro.obtenerMunicipiosOrdenados());
        String mun = centro.resolverNombreMunicipio(leerTextoSoloLetras(sc, "Escriba el nombre del municipio: "));
        List<Paquete> lista = centro.obtenerPaquetesPorMunicipio(mun);
        if (lista.isEmpty()) {
            System.out.println("-> No hay paquetes registrados hacia '" + mun + "'.");
        } else {
            System.out.println("-> Total paquetes hacia " + mun + ": " + lista.size());
            for (Paquete p : lista) {
                System.out.println("   * " + p);
            }
        }
    }

    private static void opcionDespacharLlegada(CentroDistribucion centro) {
        System.out.println("\n--- 5. DESPACHO POR ORDEN DE LLEGADA (FIFO - ArrayDeque) ---");
        Paquete candidato = centro.verSiguienteEnColaLlegada();
        if (candidato == null) {
            System.out.println("[!] No hay paquetes pendientes por despachar.");
            return;
        }
        System.out.println("Siguiente en fila por orden de llegada: " + candidato);
        Repartidor rep = centro.buscarRepartidorDisponibleParaZona(candidato.getDestino());
        if (rep == null) {
            throw new IllegalArgumentException("No se puede despachar '" + candidato.getCodigo()
                    + "': No hay ningún repartidor DISPONIBLE en la zona '" + candidato.getDestino()
                    + "'. Registre una entrega pendiente (opción 14) o libere uno (opción 8).");
        }
        Paquete atendido = centro.atenderSiguientePorOrdenLlegada(rep);
        mostrarDespacho(atendido, rep);
    }

    private static void opcionDespacharPrioridad(CentroDistribucion centro) {
        System.out.println("\n--- 6. DESPACHO PRIORITARIO (PriorityQueue: 1°Prio, 2°Tiempo, 3°Código) ---");
        Paquete candidato = centro.verSiguienteEnColaPrioridad();
        if (candidato == null) {
            System.out.println("[!] No hay paquetes pendientes por despachar.");
            return;
        }
        System.out.println("Siguiente paquete más prioritario: " + candidato);
        Repartidor rep = centro.buscarRepartidorDisponibleParaZona(candidato.getDestino());
        if (rep == null) {
            throw new IllegalArgumentException("No se puede despachar '" + candidato.getCodigo()
                    + "': No hay ningún repartidor DISPONIBLE en la zona '" + candidato.getDestino()
                    + "'. Registre una entrega pendiente (opción 14) o libere uno (opción 8).");
        }
        Paquete atendido = centro.atenderSiguientePorPrioridad(rep);
        mostrarDespacho(atendido, rep);
    }

    private static void mostrarDespacho(Paquete atendido, Repartidor rep) {
        System.out.println("[DESPACHADO CON ÉXITO] -> " + atendido);
        System.out.println("[REPARTIDOR ASIGNADO]  -> " + rep.getNombre() + " (" + rep.getIdentificacion()
                + ") pasó a ocupado y lleva el paquete en ruta.");
        System.out.println("   Cuando lo entregue, regístrelo con la opción 14 para liberarlo.");
    }

    private static void opcionRegistrarEntrega(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 14. REGISTRAR ENTREGA ---");
        Map<String, Paquete> enRuta = centro.getPaquetesEnRuta();
        if (enRuta.isEmpty()) {
            System.out.println("[!] No hay paquetes en ruta. Despache uno con las opciones 5 o 6.");
            return;
        }
        System.out.println("Paquetes en ruta:");
        for (Map.Entry<String, Paquete> e : enRuta.entrySet()) {
            System.out.println("   * " + e.getKey() + " lleva " + e.getValue().getCodigo()
                    + " hacia " + e.getValue().getDestino());
        }
        String id = leerCodigoAlfanumerico(sc, "ID del repartidor que entregó (ej. REP-101): ");
        Paquete entregado = centro.registrarEntrega(id);
        if (entregado == null) {
            throw new IllegalArgumentException("El repartidor '" + id + "' no existe o no tiene ningún paquete en ruta.");
        }
        System.out.println("[ENTREGADO] -> " + entregado.getCodigo() + " agregado al historial.");
        System.out.println("[LIBERADO]  -> " + centro.consultarRepartidorPorId(id).getNombre() + " vuelve a estar disponible.");
    }

    private static void opcionReportesOrdenados(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 7. REPORTES ORDENADOS ---");
        System.out.println("1. Orden original de llegada");
        System.out.println("2. Por Código ascendente (Orden natural - Comparable)");
        System.out.println("3. Por Prioridad de mayor a menor (Comparator)");
        System.out.println("4. Por Peso de mayor a menor (Comparator)");
        System.out.println("5. Por Tiempo estimado de menor a mayor (Comparator)");

        int crit = leerEnteroEnRango(sc, "Seleccione el reporte (1-5): ", 1, 5);
        int max = centro.getPaquetesOrdenLlegada().size();
        if (max == 0) {
            System.out.println("[!] No hay paquetes registrados.");
            return;
        }
        int cantidad = leerEnteroEnRango(sc, "¿Cuántos paquetes desea visualizar? (1 a " + max + "): ", 1, max);

        List<Paquete> reporte;
        switch (crit) {
            case 1: reporte = centro.getPaquetesOrdenLlegada(); break;
            case 2: reporte = centro.obtenerPaquetesOrdenadosPorCodigo(); break;
            case 3: reporte = centro.obtenerPaquetesOrdenadosPorPrioridad(); break;
            case 4: reporte = centro.obtenerPaquetesOrdenadosPorPeso(); break;
            default: reporte = centro.obtenerPaquetesOrdenadosPorTiempo(); break;
        }

        for (int i = 0; i < cantidad; i++) {
            System.out.println("   " + (i + 1) + ". " + reporte.get(i));
        }
    }

    private static void opcionGestionarRepartidores(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 8. GESTIÓN DE REPARTIDORES ---");
        for (Repartidor r : centro.getRepartidoresPorId().values()) {
            Paquete enRuta = centro.obtenerPaqueteEnRuta(r.getIdentificacion());
            System.out.println("   * " + r + (enRuta != null ? "  -> en ruta con " + enRuta.getCodigo() : ""));
        }
        System.out.println("\nAcciones: [1] Registrar nuevo repartidor | [2] Cambiar disponibilidad | [0] Volver");
        int sub = leerEnteroEnRango(sc, "Elija acción (0-2): ", 0, 2);

        if (sub == 1) {
            String id = leerCodigoAlfanumerico(sc, "Identificación única (ej. REP-800): ");
            while (centro.consultarRepartidorPorId(id) != null) {
                error("Ya existe un repartidor con la identificación '" + id + "'.");
                id = leerCodigoAlfanumerico(sc, "Identificación única (ej. REP-800): ");
            }
            String nombre = leerTextoSoloLetras(sc, "Nombre completo (solo letras): ");
            String zona = centro.resolverNombreMunicipio(leerTextoSoloLetras(sc, "Zona/Municipio asignado (solo letras): "));
            boolean disp = leerSiNo(sc, "¿Está disponible actualmente? (s/n): ");

            Repartidor nuevo = new Repartidor(id, nombre, zona, disp);
            centro.registrarRepartidor(nuevo);
            System.out.println("[OK] Repartidor registrado: " + nuevo);
        } else if (sub == 2) {
            String id = leerCodigoAlfanumerico(sc, "Ingrese ID del repartidor a modificar (ej. REP-101): ");
            Repartidor rep = centro.consultarRepartidorPorId(id);
            if (rep == null) {
                throw new IllegalArgumentException("No existe ningún repartidor con ID '" + id + "'.");
            }
            Paquete enRuta = centro.obtenerPaqueteEnRuta(rep.getIdentificacion());
            if (enRuta != null) {
                throw new IllegalArgumentException(rep.getNombre() + " tiene en ruta el paquete " + enRuta.getCodigo()
                        + ". Registre primero la entrega con la opción 14.");
            }
            boolean disp = leerSiNo(sc, "¿Marcar como disponible? (s/n): ");
            rep.setDisponible(disp);
            System.out.println("[OK] Estado actualizado: " + rep);
        }
    }

    private static void opcionHistorialEntregas(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 9. HISTORIAL DE ENTREGAS (Lista Simplemente Enlazada Propia) ---");
        ColeccionSimple<Paquete> hist = centro.getHistorialEntregas();
        System.out.println("Total entregados: " + hist.tamano() + " | ¿Vacía?: " + hist.estaVacia());

        if (hist.estaVacia()) {
            System.out.println("   (Aún no hay entregas. Despache con las opciones 5 o 6 y registre la entrega con la 14).");
            return;
        }

        int idx = 0;
        for (Paquete p : hist) {
            System.out.println("   [" + (idx++) + "] -> " + p);
        }

        System.out.println("\nOperaciones lista propia: [1] Obtener por índice | [2] Eliminar paquete del historial | [0] Volver");
        int sub = leerEnteroEnRango(sc, "Elija operación (0-2): ", 0, 2);
        if (sub == 1) {
            int pos = leerEnteroEnRango(sc, "Índice a consultar (0 a " + (hist.tamano() - 1) + "): ", 0, hist.tamano() - 1);
            System.out.println("-> Elemento obtenido: " + hist.obtener(pos));
        } else if (sub == 2) {
            String cod = leerCodigoAlfanumerico(sc, "Código del paquete a eliminar del historial (ej. PQ105): ");
            Paquete referencia = centro.consultarPaquetePorCodigo(cod);
            boolean eliminado = referencia != null && hist.eliminar(referencia);
            System.out.println(eliminado
                    ? "[OK] Paquete '" + cod + "' eliminado de la lista enlazada."
                    : "[!] El paquete '" + cod + "' no se encontraba en el historial.");
        }
    }

    private static void opcionRecursivaPesoMunicipio(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 10. RECURSIVIDAD 1: PESO TOTAL POR MUNICIPIO ---");
        String mun = centro.resolverNombreMunicipio(leerTextoSoloLetras(sc, "Ingrese municipio a calcular (solo letras, ej. Armenia): "));
        double total = centro.calcularPesoTotalPorMunicipio(mun);
        System.out.printf("-> Peso total hacia %s: %.2f kg%n", mun, total);
    }

    private static void opcionRecursivaContarPrioridad(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 11. RECURSIVIDAD 2: CONTAR PAQUETES CON PRIORIDAD >= X ---");
        int prioMin = leerEnteroEnRango(sc, "Ingrese prioridad mínima (entero de 1 a 5): ", 1, 5);
        int conteo = centro.contarPaquetesConPrioridadMinima(prioMin);
        System.out.println("-> Cantidad de paquetes con prioridad >= " + prioMin + ": " + conteo + " paquetes.");
    }

    private static void opcionBusquedaBinaria(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 12. BÚSQUEDA BINARIA POR CÓDIGO ---");
        String cod = leerCodigoAlfanumerico(sc, "Ingrese código a localizar (ej. PQ731, PQ105): ");
        List<Paquete> ordenados = centro.obtenerPaquetesOrdenadosPorCodigo();
        Paquete hallado = centro.buscarPaqueteBinariaPorCodigo(ordenados, cod);
        System.out.println(hallado != null
                ? "-> Localizado por Búsqueda Binaria: " + hallado
                : "-> El código '" + cod + "' no existe en la colección.");
    }

    // =========================================================================
    // MÉTODOS VALIDADORES DE ENTRADA (LANZAN EXCEPCIÓN SI EL TIPO ES INCORRECTO)
    // =========================================================================

    // Lee una línea; si se acabó la entrada termina el programa de forma ordenada
    private static String leerLinea(Scanner sc) {
        try {
            return sc.nextLine();
        } catch (NoSuchElementException e) {
            throw new FinDeEntrada();
        }
    }

    // Lee un campo de una operación; escribir 'x' cancela la operación y vuelve al menú
    private static String leerCampo(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        String entrada = leerLinea(sc).trim();
        if (entrada.equalsIgnoreCase("x")) {
            throw new OperacionCancelada();
        }
        return entrada;
    }

    private static void error(String mensaje) {
        System.out.println("   [!] " + mensaje + " Intente de nuevo (o 'x' para cancelar).");
    }

    // Todos los lectores repiten la pregunta hasta recibir un dato válido, sin perder lo ya escrito

    private static String leerTextoSoloLetras(Scanner sc, String mensaje) {
        while (true) {
            String entrada = leerCampo(sc, mensaje);
            if (entrada.isEmpty()) {
                error("El campo no puede estar vacío.");
            } else if (!entrada.matches("^[\\p{L}\\s]+$")) {
                error("'" + entrada + "' no es válido: solo se permiten letras y espacios.");
            } else {
                return entrada;
            }
        }
    }

    private static String leerCodigoAlfanumerico(Scanner sc, String mensaje) {
        while (true) {
            String entrada = leerCampo(sc, mensaje).toUpperCase();
            if (entrada.isEmpty()) {
                error("El código no puede estar vacío.");
            } else if (!entrada.matches("^(?=.*[\\p{L}])(?=.*[0-9])[\\p{L}0-9-]+$")) {
                error("'" + entrada + "' no es válido: debe combinar letras y números (ej. PQ731 o REP-101).");
            } else {
                return entrada;
            }
        }
    }

    private static double leerDoublePositivo(Scanner sc, String mensaje) {
        while (true) {
            String entrada = leerCampo(sc, mensaje).replace(',', '.');
            try {
                double valor = Double.parseDouble(entrada);
                if (Double.isNaN(valor) || Double.isInfinite(valor) || valor <= 0) {
                    error("El valor debe ser un número mayor que cero.");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                error("'" + entrada + "' no es un número (ej. 4.5).");
            }
        }
    }

    private static int leerEnteroPositivo(Scanner sc, String mensaje) {
        while (true) {
            String entrada = leerCampo(sc, mensaje);
            try {
                int valor = Integer.parseInt(entrada);
                if (valor <= 0) {
                    error("El número debe ser un entero mayor que cero.");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                error("'" + entrada + "' no es un número entero válido.");
            }
        }
    }

    private static int leerEnteroEnRango(Scanner sc, String mensaje, int min, int max) {
        while (true) {
            String entrada = leerCampo(sc, mensaje);
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                error("El número debe estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                error("'" + entrada + "' no es un número entero válido.");
            }
        }
    }

    // Igual que leerEnteroEnRango pero para el menú principal (aquí 'x' no aplica)
    private static int leerOpcion(Scanner sc, String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String entrada = leerLinea(sc).trim();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // se informa abajo
            }
            System.out.println("   [!] Opción inválida: escriba un número entre " + min + " y " + max + ".");
        }
    }

    private static boolean leerSiNo(Scanner sc, String mensaje) {
        while (true) {
            String entrada = leerCampo(sc, mensaje).toLowerCase();
            if (entrada.equals("s") || entrada.equals("si") || entrada.equals("sí")) {
                return true;
            }
            if (entrada.equals("n") || entrada.equals("no")) {
                return false;
            }
            error("Responda 's' (sí) o 'n' (no).");
        }
    }

    private static void cargarDatosBase(CentroDistribucion c) {
        String[][] reps = {
            {"REP-101", "Carlos Gómez", "Armenia"}, {"REP-205", "Laura Martínez", "Salento"},
            {"REP-309", "Andrés Quintero", "Calarcá"}, {"REP-412", "Diana Loaiza", "Circasia"},
            {"REP-518", "Mateo Cardona", "Filandia"}, {"REP-624", "Valentina Ríos", "Montenegro"},
            {"REP-730", "Jorge Arias", "Armenia"}
        };
        for (String[] r : reps) {
            c.registrarRepartidor(new Repartidor(r[0], r[1], r[2], true));
        }

        Object[][] paqs = {
            {"PQ731","Armenia",4.5,5,45}, {"PQ105","Salento",3.0,5,25}, {"PQ942","Armenia",7.2,4,20},
            {"PQ318","Calarcá",12.0,3,30}, {"PQ567","Salento",2.3,2,40}, {"PQ829","Armenia",6.8,4,35},
            {"PQ420","Circasia",1.5,3,30}, {"PQ214","Filandia",8.4,5,25}, {"PQ653","Montenegro",5.1,1,50},
            {"PQ190","Calarcá",9.6,4,15}, {"PQ884","Armenia",3.2,2,22}, {"PQ512","Salento",18.7,5,60},
            {"PQ376","Circasia",4.0,3,28}, {"PQ901","Filandia",2.9,1,45}, {"PQ147","Montenegro",11.3,4,35},
            {"PQ765","Armenia",5.5,3,18}, {"PQ298","Calarcá",7.4,2,27}, {"PQ619","Salento",1.8,5,25},
            {"PQ483","Circasia",6.2,4,32}, {"PQ850","Filandia",10.1,2,48}, {"PQ112","Montenegro",3.7,3,29},
            {"PQ975","Armenia",14.2,5,55}, {"PQ341","Calarcá",2.1,1,20}, {"PQ708","Salento",8.9,4,38},
            {"PQ259","Circasia",5.0,2,24}, {"PQ690","Filandia",13.5,5,42}, {"PQ437","Montenegro",4.8,3,31},
            {"PQ816","Armenia",9.0,4,26}, {"PQ539","Calarcá",6.4,5,19}, {"PQ174","Salento",2.6,1,33}
        };
        for (Object[] p : paqs) {
            c.registrarPaquete(new Paquete((String)p[0], (String)p[1], (double)p[2], (int)p[3], (int)p[4]));
        }
    }
}

