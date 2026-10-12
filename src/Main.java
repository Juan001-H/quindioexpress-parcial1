import estructuras.ColeccionSimple;
import java.util.List;
import java.util.Scanner;
import modelo.Paquete;
import modelo.Repartidor;
import servicio.CentroDistribucion;

public class Main {

    public static void main(String[] args) {
        CentroDistribucion centro = new CentroDistribucion();
        cargarDatosBase(centro);

        Scanner sc = new Scanner(System.in);
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
            System.out.println(" 7. Reportes ordenados (Comparable / Comparator)");
            System.out.println(" 0. Salir");
            System.out.println("=====================================================================================");

            opcion = leerEnteroEnRango(sc, "-> Elija una opción (0-13): ", 0, 13);

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
                    case 0:
                        System.out.println("Programa finalizado correctamente.");
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("\n[EXCEPCIÓN DE VALIDACIÓN]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[ERROR INESPERADO]: " + e.getMessage());
            }

            if (opcion != 0) {
                System.out.print("\n[Presione ENTER para volver al menú]");
                sc.nextLine();
            }
        }
        sc.close();
    }

    // =========================================================================
    // FUNCIONALIDADES INTERACTIVAS CON VALIDACIÓN INMEDIATA CAMPO POR CAMPO
    // =========================================================================

    private static void opcionRegistrarPaquete(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 1. REGISTRAR PAQUETE NUEVO ---");

        // 1. Valida formato de código inmediatamente y verifica duplicado antes de seguir
        String codigo = leerCodigoAlfanumerico(sc, "Código del paquete (letras y números, ej. PQ888): ");
        if (centro.consultarPaquetePorCodigo(codigo) != null) {
            throw new IllegalArgumentException("Rechazado: Ya existe un paquete registrado con el código '" + codigo + "'.");
        }

        // 2. Valida que el municipio solo tenga letras (si escriben "65", lanza error de inmediato)
        String destino = leerTextoSoloLetras(sc, "Municipio destino (solo letras, ej. Armenia, Salento): ");

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
        String mun = leerTextoSoloLetras(sc, "Escriba el nombre del municipio: ");
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
                    + "'. (Puede liberar uno en la opción 8).");
        }
        Paquete atendido = centro.atenderSiguientePorOrdenLlegada(rep);
        System.out.println("[DESPACHADO CON ÉXITO] -> " + atendido);
        System.out.println("[REPARTIDOR ASIGNADO]  -> " + rep.getNombre() + " (" + rep.getIdentificacion() + ") pasó a ocupado.");
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
                    + "'. (Puede liberar uno en la opción 8).");
        }
        Paquete atendido = centro.atenderSiguientePorPrioridad(rep);
        System.out.println("[DESPACHADO CON ÉXITO] -> " + atendido);
        System.out.println("[REPARTIDOR ASIGNADO]  -> " + rep.getNombre() + " (" + rep.getIdentificacion() + ") pasó a ocupado.");
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
            System.out.println("   * " + r);
        }
        System.out.println("\nAcciones: [1] Registrar nuevo repartidor | [2] Cambiar disponibilidad | [0] Volver");
        int sub = leerEnteroEnRango(sc, "Elija acción (0-2): ", 0, 2);

        if (sub == 1) {
            String id = leerCodigoAlfanumerico(sc, "Identificación única (ej. REP-800): ");
            if (centro.consultarRepartidorPorId(id) != null) {
                throw new IllegalArgumentException("Rechazado: Ya existe un repartidor con la identificación '" + id + "'.");
            }
            String nombre = leerTextoSoloLetras(sc, "Nombre completo (solo letras): ");
            String zona = leerTextoSoloLetras(sc, "Zona/Municipio asignado (solo letras): ");
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
            System.out.println("   (Aún no hay paquetes entregados. Despache uno con las opciones 5 o 6).");
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
            Paquete referencia = new Paquete(cod, "Armenia", 1.0, 1, 1);
            boolean eliminado = hist.eliminar(referencia);
            System.out.println(eliminado
                    ? "[OK] Paquete '" + cod + "' eliminado de la lista enlazada."
                    : "[!] El paquete '" + cod + "' no se encontraba en el historial.");
        }
    }

    private static void opcionRecursivaPesoMunicipio(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 10. RECURSIVIDAD 1: PESO TOTAL POR MUNICIPIO ---");
        String mun = leerTextoSoloLetras(sc, "Ingrese municipio a calcular (solo letras, ej. Armenia): ");
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

    private static String leerTextoSoloLetras(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        String entrada = sc.nextLine().trim();
        if (entrada.isEmpty()) {
            throw new IllegalArgumentException("El campo de texto no puede estar vacío.");
        }
        if (!entrada.matches("^[\\p{L}\\s]+$")) {
            throw new IllegalArgumentException("Valor inválido ('" + entrada + "'): Este campo exige TEXTO (solo letras), no se permiten números ni símbolos.");
        }
        return entrada;
    }

    private static String leerCodigoAlfanumerico(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        String entrada = sc.nextLine().trim().toUpperCase();
        if (entrada.isEmpty()) {
            throw new IllegalArgumentException("El código/identificación no puede estar vacío.");
        }
        if (!entrada.matches("^(?=.*[\\p{L}])(?=.*[0-9])[\\p{L}0-9-]+$")) {
            throw new IllegalArgumentException("Código inválido ('" + entrada + "'): Debe combinar letras y números (ej. PQ731 o REP-101), no puede ser solo números.");
        }
        return entrada;
    }

    private static double leerDoublePositivo(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        String entrada = sc.nextLine().trim().replace(',', '.');
        double valor;
        try {
            valor = Double.parseDouble(entrada);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido ('" + entrada + "'): Se esperaba un número decimal (ej. 4.5).");
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor <= 0) {
            throw new IllegalArgumentException("El valor debe ser estrictamente mayor que cero (> 0).");
        }
        return valor;
    }

    private static int leerEnteroPositivo(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        String entrada = sc.nextLine().trim();
        int valor;
        try {
            valor = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido ('" + entrada + "'): Se esperaba un número entero positivo.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("El número entero debe ser mayor que cero (> 0).");
        }
        return valor;
    }

    private static int leerEnteroEnRango(Scanner sc, String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String entrada = sc.nextLine().trim();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("[!] Error: El número debe estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("[!] Error: '" + entrada + "' no es un número entero válido.");
            }
        }
    }

    private static boolean leerSiNo(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        String entrada = sc.nextLine().trim().toLowerCase();
        if (entrada.equals("s") || entrada.equals("si") || entrada.equals("sí")) {
            return true;
        }
        if (entrada.equals("n") || entrada.equals("no")) {
            return false;
        }
        throw new IllegalArgumentException("Respuesta inválida ('" + entrada + "'): Debe responder 's' (sí) o 'n' (no).");
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

