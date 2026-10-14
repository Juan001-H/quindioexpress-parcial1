import estructuras.ColeccionSimple;
import estructuras.ListaSimplementeEnlazada;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import modelo.Paquete;
import modelo.Repartidor;
import servicio.CentroDistribucion;

public class Main {

    // Se lanza cuando el usuario escribe 'x' para cancelar la operación a medias
    private static class OperacionCancelada extends RuntimeException {
        private static final long serialVersionUID = 1L;

        OperacionCancelada() {
            super("Operación cancelada por el usuario.");
        }
    }

    // Se lanza si se fuerza el cierre de la consola (Ctrl+C / Ctrl+D)
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
            System.out.println("\n\nSistema cerrado.");
        }
        sc.close();
    }

    private static void ejecutarMenu(CentroDistribucion centro, Scanner sc) {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n==========================================================================================");
            System.out.println("                   QUINDIO EXPRESS - SISTEMA DE GESTION DE OPERACIONES");
            System.out.println("                   (Paquetes en Bodega: " + centro.obtenerCantidadPendientes() + " | Total Histórico: " + centro.getPaquetesOrdenLlegada().size() + ")");
            System.out.println("==========================================================================================");
            
            System.out.println("\n [ GESTION OPERATIVA ]                         [ DESPACHOS Y ENTREGAS ]");
            System.out.println("  1. Registrar un paquete nuevo                 5. Realizar despacho (Llegada/Prioridad)");
            System.out.println("  2. Buscar paquete por código O(1)             6. Administrar repartidores (Ver/Agregar)");
            System.out.println("  3. Ver zonas de destino activas               7. Registrar una entrega finalizada");
            System.out.println("  4. Consultar paquetes de una zona             8. Ver historial de entregas (Lista Propia)");
            System.out.println("  9. Ver todos los paquetes pendientes");
            System.out.println(" 10. Corregir/Eliminar paquete del sistema");
            
            System.out.println("\n [ ANALISIS ALGORITMICO ]                      [ REPORTES Y PRUEBAS DEL SISTEMA ]");
            System.out.println(" 11. (Recursivo) Peso total por municipio      15. Generar reportes de ordenamiento");
            System.out.println(" 12. (Recursivo) Contar por prioridad alta     16. Lista propia: eliminar (6 casos) y obtener");
            System.out.println(" 13. (B. Binaria) Buscar paquete en lista");
            System.out.println(" 14. (Divide/Vencerás) Hallar el más pesado");
            System.out.println("------------------------------------------------------------------------------------------");
            System.out.println("  0. Salir de la aplicacion  |  (En cualquier momento escriba 'x' para cancelar accion)");
            System.out.println("==========================================================================================\n");

            opcion = leerOpcion(sc, "-> ¿Que deseas hacer? (0-16): ", 0, 16);

            try {
                switch (opcion) {
                    case 1: opcionRegistrarPaquete(centro, sc); break;
                    case 2: opcionConsultarPorCodigo(centro, sc); break;
                    case 3:
                        System.out.println("\n--- 3. MUNICIPIOS CON ENTREGAS ---");
                        System.out.println("-> Sin orden específico (HashSet):  " + centro.obtenerMunicipiosConEntregas());
                        System.out.println("-> En orden alfabético (TreeSet):   " + centro.obtenerMunicipiosOrdenados());
                        break;
                    case 4: opcionPaquetesPorMunicipio(centro, sc); break;
                    case 5: opcionRealizarDespacho(centro, sc); break;
                    case 6: opcionGestionarRepartidores(centro, sc); break;
                    case 7: opcionRegistrarEntrega(centro, sc); break;
                    case 8: opcionHistorialEntregas(centro, sc); break;
                    case 9: opcionVerPendientes(centro); break;
                    case 10: opcionEliminarPendiente(centro, sc); break;
                    case 11: opcionRecursivaPesoMunicipio(centro, sc); break;
                    case 12: opcionRecursivaContarPrioridad(centro, sc); break;
                    case 13: opcionBusquedaBinaria(centro, sc); break;
                    case 14:
                        System.out.println("\n--- 14. EL PAQUETE MAS PESADO (DIVIDE Y VENCERAS) ---");
                        System.out.println("-> Resultado: " + centro.obtenerPaqueteMayorPesoDivideYVenceras());
                        break;
                    case 15: opcionReportesOrdenados(centro, sc); break;
                    case 16: opcionPruebaEliminacionLista(sc); break;
                    case 0:
                        System.out.println("Cerrando sesión en QuindíoExpress. ¡Hasta luego!");
                        break;
                }
            } catch (OperacionCancelada e) {
                System.out.println("\n[AVISO]: " + e.getMessage());
            } catch (FinDeEntrada e) {
                throw e;
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERROR DE DATOS]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[ERROR DEL SISTEMA]: " + e.getMessage());
            }

            if (opcion != 0 && opcion != 16) {
                esperarEnter(sc, "\n[Presione ENTER para continuar]");
            }
        }
    }

    // =========================================================================
    // LÓGICA DE LAS OPCIONES DEL MENÚ
    // =========================================================================

    private static void opcionRegistrarPaquete(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 1. REGISTRAR PAQUETE NUEVO ---");

        String codigo = leerCodigoAlfanumerico(sc, "Código del paquete (ej. PQ888): ");
        while (centro.consultarPaquetePorCodigo(codigo) != null) {
            error("Ya tenemos un paquete registrado con el código '" + codigo + "'.");
            codigo = leerCodigoAlfanumerico(sc, "Código del paquete (ej. PQ888): ");
        }

        String destino = centro.resolverNombreMunicipio(
                leerTextoSoloLetras(sc, "Municipio de destino (ej. Armenia): "));

        double peso = leerDoublePositivo(sc, "Peso en kilogramos (ej. 4.5): ");
        int prioridad = leerEnteroEnRango(sc, "Nivel de prioridad (del 1 al 5): ", 1, 5);
        int tiempo = leerEnteroPositivo(sc, "Tiempo estimado en minutos: ");

        Paquete nuevo = new Paquete(codigo, destino, peso, prioridad, tiempo);
        centro.registrarPaquete(nuevo);
        System.out.println("\n[ÉXITO] El paquete quedó guardado en el sistema:");
        System.out.println("-> " + nuevo);
    }

    private static void opcionConsultarPorCodigo(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 2. BUSCAR PAQUETE POR CÓDIGO ---");
        String codigo = leerCodigoAlfanumerico(sc, "Escriba el código exacto a buscar: ");
        Paquete encontrado = centro.consultarPaquetePorCodigo(codigo);
        
        if (encontrado != null) {
            System.out.println("\n-> Información encontrada: " + encontrado);
        } else {
            System.out.println("\n-> No tenemos ningún paquete con el código '" + codigo + "'.");
        }
    }

    private static void opcionPaquetesPorMunicipio(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 4. PAQUETES POR ZONA ---");
        System.out.println("Las zonas activas son: " + centro.obtenerMunicipiosOrdenados());
        String mun = centro.resolverNombreMunicipio(leerTextoSoloLetras(sc, "¿De qué municipio quieres ver los paquetes?: "));
        
        List<Paquete> lista = centro.obtenerPaquetesPorMunicipio(mun);
        if (lista.isEmpty()) {
            System.out.println("\n-> Todo limpio. No hay paquetes para '" + mun + "'.");
        } else {
            System.out.println("\n-> Tenemos " + lista.size() + " paquetes para " + mun + ":");
            for (Paquete p : lista) {
                System.out.println("   * " + p);
            }
        }
    }

    // NUEVO SUBMENÚ QUE AGRUPA LOS DESPACHOS
    private static void opcionRealizarDespacho(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 5. REALIZAR DESPACHO ---");
        System.out.println("1. Despacho Normal (Por estricto orden de llegada)");
        System.out.println("2. Despacho Prioritario (Por nivel de urgencia)");
        System.out.println("0. Cancelar y volver al menú principal");

        int sub = leerEnteroEnRango(sc, "\n-> ¿Qué tipo de despacho deseas hacer? (0-2): ", 0, 2);
        if (sub == 1) {
            opcionDespacharLlegada(centro);
        } else if (sub == 2) {
            opcionDespacharPrioridad(centro);
        }
    }

    private static void opcionDespacharLlegada(CentroDistribucion centro) {
        System.out.println("\n[ DESPACHO POR ORDEN DE LLEGADA ]");
        Paquete candidato = centro.verSiguienteEnColaLlegada();
        if (candidato == null) {
            System.out.println("\n[INFO] La fila está vacía, no hay nada por despachar.");
            return;
        }
        
        System.out.println("El que sigue en la fila es: " + candidato);
        Repartidor rep = centro.buscarRepartidorDisponibleParaZona(candidato.getDestino());
        
        if (rep == null) {
            throw new IllegalArgumentException("No podemos sacar el '" + candidato.getCodigo()
                    + "' porque no hay repartidores libres en '" + candidato.getDestino()
                    + "'. Ve a la opción 6 para crear uno o a la 7 para liberar uno ocupado.");
        }
        
        Paquete atendido = centro.atenderSiguientePorOrdenLlegada(rep);
        mostrarDespacho(atendido, rep);
    }

    private static void opcionDespacharPrioridad(CentroDistribucion centro) {
        System.out.println("\n[ DESPACHO PRIORITARIO ]");
        Paquete candidato = centro.verSiguienteEnColaPrioridad();
        if (candidato == null) {
            System.out.println("\n[INFO] La fila está vacía, no hay nada por despachar.");
            return;
        }
        
        System.out.println("El paquete más urgente ahorita es: " + candidato);
        Repartidor rep = centro.buscarRepartidorDisponibleParaZona(candidato.getDestino());
        
        if (rep == null) {
            throw new IllegalArgumentException("No podemos sacar el '" + candidato.getCodigo()
                    + "' porque no hay repartidores libres en '" + candidato.getDestino()
                    + "'. Ve a la opción 6 para crear uno o a la 7 para liberar uno ocupado.");
        }
        
        Paquete atendido = centro.atenderSiguientePorPrioridad(rep);
        mostrarDespacho(atendido, rep);
    }

    private static void mostrarDespacho(Paquete atendido, Repartidor rep) {
        System.out.println("\n[DESPACHO CONFIRMADO]");
        System.out.println("-> Paquete: " + atendido);
        System.out.println("-> Repartidor: " + rep.getNombre() + " (" + rep.getIdentificacion() + ")");
        System.out.println("Nota: El repartidor ahora aparece como ocupado. Usa la opción 7 cuando regrese.");
    }

    private static void opcionGestionarRepartidores(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 6. ADMINISTRACIÓN DE REPARTIDORES ---");
        for (Repartidor r : centro.getRepartidoresPorId().values()) {
            Paquete enRuta = centro.obtenerPaqueteEnRuta(r.getIdentificacion());
            System.out.println("   * " + r + (enRuta != null ? "  -> (Repartiendo el " + enRuta.getCodigo() + ")" : ""));
        }
        System.out.println("\nOpciones: [1] Agregar uno nuevo | [2] Cambiar su estado | [0] Volver al menú");
        int sub = leerEnteroEnRango(sc, "¿Qué deseas hacer? (0-2): ", 0, 2);

        if (sub == 1) {
            String id = leerCodigoAlfanumerico(sc, "ID único (ej. REP-800): ");
            while (centro.consultarRepartidorPorId(id) != null) {
                error("Ese ID ya lo tiene otro repartidor.");
                id = leerCodigoAlfanumerico(sc, "ID único (ej. REP-800): ");
            }
            String nombre = leerTextoSoloLetras(sc, "Nombre completo: ");
            String zona = centro.resolverNombreMunicipio(leerTextoSoloLetras(sc, "¿En qué municipio trabaja?: "));
            boolean disp = leerSiNo(sc, "¿Está disponible para trabajar ya mismo? (s/n): ");

            Repartidor nuevo = new Repartidor(id, nombre, zona, disp);
            centro.registrarRepartidor(nuevo);
            System.out.println("\n[ÉXITO] Repartidor guardado en la base de datos.");
        } else if (sub == 2) {
            String id = leerCodigoAlfanumerico(sc, "Escribe el ID del repartidor que vas a editar (ej. REP-101): ");
            Repartidor rep = centro.consultarRepartidorPorId(id);
            if (rep == null) {
                throw new IllegalArgumentException("No encontré a nadie con ese ID.");
            }
            Paquete enRuta = centro.obtenerPaqueteEnRuta(rep.getIdentificacion());
            if (enRuta != null) {
                throw new IllegalArgumentException("No le puedes cambiar el estado ahorita porque está ocupado entregando el " + enRuta.getCodigo() + ". Ve a la opción 7.");
            }
            boolean disp = leerSiNo(sc, "¿Lo marcamos como DISPONIBLE? (s/n): ");
            rep.setDisponible(disp);
            System.out.println("\n[ÉXITO] Listo, estado actualizado.");
        }
    }

    private static void opcionRegistrarEntrega(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 7. REGISTRAR UNA ENTREGA FINALIZADA ---");
        Map<String, Paquete> enRuta = centro.getPaquetesEnRuta();
        
        if (enRuta.isEmpty()) {
            System.out.println("\n[INFO] Ahorita mismo no hay ningún repartidor en la calle.");
            return;
        }
        
        System.out.println("Repartidores actualmente en ruta:");
        for (Map.Entry<String, Paquete> e : enRuta.entrySet()) {
            System.out.println("   * ID: " + e.getKey() + " (Lleva el " + e.getValue().getCodigo()
                    + " a " + e.getValue().getDestino() + ")");
        }
        
        String id = leerCodigoAlfanumerico(sc, "\n¿Qué ID de repartidor acaba de regresar?: ");
        Paquete entregado = centro.registrarEntrega(id);
        
        if (entregado == null) {
            throw new IllegalArgumentException("Ese repartidor no existe o no estaba en ruta con ningún paquete.");
        }
        
        System.out.println("\n[OPERACIÓN EXITOSA]");
        System.out.println("-> El paquete " + entregado.getCodigo() + " se movió al historial oficial.");
        System.out.println("-> El repartidor quedó libre para recibir otra asignación.");
    }

    private static void opcionHistorialEntregas(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 8. HISTORIAL OFICIAL DE ENTREGAS ---");
        ColeccionSimple<Paquete> hist = centro.getHistorialEntregas();
        System.out.println("Total de paquetes ya entregados: " + hist.tamano());

        if (hist.estaVacia()) {
            System.out.println("\n   (Aún está vacío. Haz un despacho primero y luego registra la entrega).");
            return;
        }

        System.out.println("");
        int idx = 0;
        for (Paquete p : hist) {
            System.out.println("   [Entrega N°" + (++idx) + "] -> " + p);
        }
    }

    private static void opcionVerPendientes(CentroDistribucion centro) {
        System.out.println("\n--- 9. PAQUETES ACTUALMENTE EN LA BODEGA ---");
        List<Paquete> pendientes = centro.obtenerPaquetesPendientes();
        
        if (pendientes.isEmpty()) {
            System.out.println("\n-> Todo despachado. Bodega vacía.");
        } else {
            System.out.println("Total en espera: " + pendientes.size());
            for (int i = 0; i < pendientes.size(); i++) {
                System.out.println("   " + (i + 1) + ". " + pendientes.get(i));
            }
        }
    }

    private static void opcionEliminarPendiente(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 10. ELIMINAR PAQUETE DEL SISTEMA ---");
        String codBorrar = leerCodigoAlfanumerico(sc, "¿Cuál es el código del paquete que necesitas borrar? (ej. PQ105): ");
        
        if (centro.eliminarPaquetePendiente(codBorrar)) {
            System.out.println("\n[BORRADO COMPLETADO] El paquete '" + codBorrar + "' se eliminó de todas las filas.");
        } else {
            System.out.println("\n[ERROR] El paquete '" + codBorrar + "' no se puede borrar porque no existe, o ya salió de la bodega.");
        }
    }

    private static void opcionRecursivaPesoMunicipio(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 11. CALCULAR PESO TOTAL POR ZONA (RECURSIVIDAD) ---");
        String mun = centro.resolverNombreMunicipio(leerTextoSoloLetras(sc, "¿De qué municipio quieres sumar los pesos?: "));
        double total = centro.calcularPesoTotalPorMunicipio(mun);
        System.out.printf("\n-> Todo lo que va para %s suma un total de: %.2f kg%n", mun, total);
    }

    private static void opcionRecursivaContarPrioridad(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 12. FILTRO DE PRIORIDAD (RECURSIVIDAD) ---");
        int prioMin = leerEnteroEnRango(sc, "¿De qué prioridad hacia arriba quieres contar? (1 a 5): ", 1, 5);
        int conteo = centro.contarPaquetesConPrioridadMinima(prioMin);
        System.out.println("\n-> Tenemos " + conteo + " paquete(s) con prioridad " + prioMin + " o superior.");
    }

    private static void opcionBusquedaBinaria(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 13. BÚSQUEDA BINARIA ---");
        String cod = leerCodigoAlfanumerico(sc, "Escribe el código a localizar: ");
        List<Paquete> ordenados = centro.obtenerPaquetesOrdenadosPorCodigo();
        Paquete hallado = centro.buscarPaqueteBinariaPorCodigo(ordenados, cod);
        
        if (hallado != null) {
            System.out.println("\n-> Lo encontré rapidísimo con búsqueda binaria: " + hallado);
        } else {
            System.out.println("\n-> Búsqueda finalizada. Ese código no existe.");
        }
    }

    private static void opcionReportesOrdenados(CentroDistribucion centro, Scanner sc) {
        System.out.println("\n--- 15. GENERAR REPORTES ORDENADOS ---");
        System.out.println("1. Tal cual fueron llegando (Orden original)");
        System.out.println("2. Por código alfabético (Comparable)");
        System.out.println("3. Por los de mayor prioridad (Comparator)");
        System.out.println("4. Por los más pesados (Comparator)");
        System.out.println("5. Por los más rápidos en entregar (Comparator)");

        int crit = leerEnteroEnRango(sc, "\n¿Cómo quieres ver la lista? (1-5): ", 1, 5);
        int max = centro.getPaquetesOrdenLlegada().size();
        if (max == 0) {
            System.out.println("\n[INFO] No hay paquetes registrados en este momento.");
            return;
        }
        int cantidad = leerEnteroEnRango(sc, "¿Cuántos quieres ver en pantalla? (1 a " + max + "): ", 1, max);

        List<Paquete> reporte;
        switch (crit) {
            case 1: reporte = centro.getPaquetesOrdenLlegada(); break;
            case 2: reporte = centro.obtenerPaquetesOrdenadosPorCodigo(); break;
            case 3: reporte = centro.obtenerPaquetesOrdenadosPorPrioridad(); break;
            case 4: reporte = centro.obtenerPaquetesOrdenadosPorPeso(); break;
            default: reporte = centro.obtenerPaquetesOrdenadosPorTiempo(); break;
        }

        System.out.println("");
        for (int i = 0; i < cantidad; i++) {
            System.out.println("   " + (i + 1) + ". " + reporte.get(i));
        }
    }

    private static void opcionPruebaEliminacionLista(Scanner sc) {
        int subOpcion = -1;
        
        while (subOpcion != 0) {
            System.out.println("\n--- 16. DEMOSTRACIÓN DE LA LISTA PROPIA ---");
            System.out.println("¿Qué caso deseas demostrar?");
            System.out.println(" 1. Caso 1: Intentar eliminar en una lista vacía");
            System.out.println(" 2. Caso 2: Eliminar el único elemento de la lista");
            System.out.println(" 3. Caso 3: Eliminar el primer elemento (la cabeza)");
            System.out.println(" 4. Caso 4: Eliminar un elemento intermedio");
            System.out.println(" 5. Caso 5: Eliminar el último elemento (la cola)");
            System.out.println(" 6. Caso 6: Intentar eliminar un elemento inexistente");
            System.out.println(" 7. Obtener un elemento según su posición");
            System.out.println(" 0. Volver al menú principal");
            
            subOpcion = leerEnteroEnRango(sc, "\n-> Elige el caso a probar (0-7): ", 0, 7);
            
            if (subOpcion == 0) {
                System.out.println("Volviendo al menú principal...");
                break;
            }
            
            ColeccionSimple<String> listaPrueba = new ListaSimplementeEnlazada<>();
            
            switch (subOpcion) {
                case 1:
                    System.out.println("\n> CASO 1: LISTA VACÍA");
                    System.out.print("  Estado inicial de la lista: "); imprimirListaPrueba(listaPrueba);
                    System.out.println("  -> Intentando ejecutar eliminar('A')...");
                    System.out.println("  -> Resultado devuelto: " + listaPrueba.eliminar("A"));
                    break;
                    
                case 2:
                    listaPrueba.agregar("A");
                    System.out.println("\n> CASO 2: ÚNICO ELEMENTO");
                    System.out.print("  Estado inicial de la lista: "); imprimirListaPrueba(listaPrueba);
                    System.out.println("  -> Intentando ejecutar eliminar('A')...");
                    System.out.println("  -> Resultado devuelto: " + listaPrueba.eliminar("A"));
                    System.out.print("  Estado final de la lista:   "); imprimirListaPrueba(listaPrueba);
                    System.out.println("  -> ¿El sistema reconoce que quedó vacía?: " + listaPrueba.estaVacia());
                    break;
                    
                case 3:
                    listaPrueba.agregar("A"); listaPrueba.agregar("B"); listaPrueba.agregar("C");
                    System.out.println("\n> CASO 3: PRIMER ELEMENTO DE LA FILA");
                    System.out.print("  Estado inicial de la lista: "); imprimirListaPrueba(listaPrueba);
                    System.out.println("  -> Intentando ejecutar eliminar('A')...");
                    System.out.println("  -> Resultado devuelto: " + listaPrueba.eliminar("A"));
                    System.out.print("  Estado final de la lista:   "); imprimirListaPrueba(listaPrueba);
                    break;
                    
                case 4:
                    listaPrueba.agregar("A"); listaPrueba.agregar("B"); listaPrueba.agregar("C");
                    System.out.println("\n> CASO 4: ELEMENTO INTERMEDIO");
                    System.out.print("  Estado inicial de la lista: "); imprimirListaPrueba(listaPrueba);
                    System.out.println("  -> Intentando ejecutar eliminar('B')...");
                    System.out.println("  -> Resultado devuelto: " + listaPrueba.eliminar("B"));
                    System.out.print("  Estado final de la lista:   "); imprimirListaPrueba(listaPrueba);
                    break;
                    
                case 5:
                    listaPrueba.agregar("A"); listaPrueba.agregar("B"); listaPrueba.agregar("C");
                    System.out.println("\n> CASO 5: ÚLTIMO ELEMENTO DE LA FILA");
                    System.out.print("  Estado inicial de la lista: "); imprimirListaPrueba(listaPrueba);
                    System.out.println("  -> Intentando ejecutar eliminar('C')...");
                    System.out.println("  -> Resultado devuelto: " + listaPrueba.eliminar("C"));
                    System.out.print("  Estado final de la lista:   "); imprimirListaPrueba(listaPrueba);
                    break;
                    
                case 6:
                    listaPrueba.agregar("A"); listaPrueba.agregar("B"); listaPrueba.agregar("C");
                    System.out.println("\n> CASO 6: ELEMENTO INEXISTENTE");
                    System.out.print("  Estado inicial de la lista: "); imprimirListaPrueba(listaPrueba);
                    System.out.println("  -> Intentando ejecutar eliminar('Z')...");
                    System.out.println("  -> Resultado devuelto: " + listaPrueba.eliminar("Z"));
                    System.out.print("  Estado final de la lista:   "); imprimirListaPrueba(listaPrueba);
                    break;

                case 7:
                    listaPrueba.agregar("A"); listaPrueba.agregar("B"); listaPrueba.agregar("C");
                    System.out.println("\n> OBTENER SEGÚN LA POSICIÓN (las posiciones empiezan en 0)");
                    System.out.print("  Estado de la lista: "); imprimirListaPrueba(listaPrueba);
                    System.out.println("  -> Tamaño de la lista: " + listaPrueba.tamano());
                    int pos = leerEnteroEnRango(sc, "  ¿Qué posición quieres obtener? (0 a "
                            + (listaPrueba.tamano() - 1) + ", o un número mayor para ver el error): ", 0, 99);
                    try {
                        System.out.println("  -> obtener(" + pos + ") devolvió: " + listaPrueba.obtener(pos));
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("  -> obtener(" + pos + ") lanzó IndexOutOfBoundsException: " + e.getMessage());
                    }
                    break;
            }
            
            esperarEnter(sc, "\n[Presione ENTER para probar otro caso]");
        }
    }

    private static void imprimirListaPrueba(ColeccionSimple<String> lista) {
        System.out.print("[");
        boolean primero = true;
        for (String s : lista) {
            if (!primero) System.out.print(" -> ");
            System.out.print(s);
            primero = false;
        }
        System.out.println("]");
    }

    // =========================================================================
    // UTILIDADES PARA LEER TEXTO Y EVITAR QUE SE DAÑE CON DATOS MALOS
    // =========================================================================

    // Pausa: solo continúa con un ENTER vacío; si escriben algo, avisa y vuelve a esperar
    private static void esperarEnter(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        while (!leerLinea(sc).trim().isEmpty()) {
            System.out.print("   [!] Primero presiona solo ENTER (sin escribir nada) para continuar: ");
        }
    }

    private static String leerLinea(Scanner sc) {
        try {
            return sc.nextLine();
        } catch (NoSuchElementException e) {
            throw new FinDeEntrada();
        }
    }

    private static String leerCampo(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        String entrada = leerLinea(sc).trim();
        if (entrada.equalsIgnoreCase("x")) {
            throw new OperacionCancelada();
        }
        return entrada;
    }

    private static void error(String mensaje) {
        System.out.println("   [!] " + mensaje + " Intenta de nuevo (o pon 'x' para salir).");
    }

    private static String leerTextoSoloLetras(Scanner sc, String mensaje) {
        while (true) {
            String entrada = leerCampo(sc, mensaje);
            if (entrada.isEmpty()) {
                error("No me dejes el texto vacío.");
            } else if (!entrada.matches("^[\\p{L}\\s]+$")) {
                error("Solo se valen letras (ni números, ni símbolos raros).");
            } else {
                return entrada;
            }
        }
    }

    private static String leerCodigoAlfanumerico(Scanner sc, String mensaje) {
        while (true) {
            String entrada = leerCampo(sc, mensaje).toUpperCase();
            if (entrada.isEmpty()) {
                error("No me dejes el código vacío.");
            } else if (!entrada.matches("^(?=.*[\\p{L}])(?=.*[0-9])[\\p{L}0-9-]+$")) {
                error("El código tiene que llevar letras y números combinados (ej. PQ123).");
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
                    error("Tiene que ser un peso mayor a cero.");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                error("Escribe un número normal, como 4.5 por ejemplo.");
            }
        }
    }

    private static int leerEnteroPositivo(Scanner sc, String mensaje) {
        while (true) {
            String entrada = leerCampo(sc, mensaje);
            try {
                int valor = Integer.parseInt(entrada);
                if (valor <= 0) {
                    error("Tiene que ser mayor a cero.");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                error("Eso no es un número entero.");
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
                error("El número tiene que estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                error("Eso no parece un número entero.");
            }
        }
    }

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
                // ignorar y mostrar mensaje
            }
            System.out.println("   [!] Opción inválida. Escoge un número del " + min + " al " + max + ".");
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
            error("Solo pon 's' para sí, o 'n' para no.");
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




