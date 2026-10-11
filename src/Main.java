import modelo.Paquete;
import modelo.Repartidor;
import servicio.CentroDistribucion;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        CentroDistribucion centro = new CentroDistribucion();

        System.out.println("=================================================================");
        System.out.println("   SISTEMA DE GESTIÓN DE OPERACIONES - QUINDÍO EXPRESS");
        System.out.println("=================================================================\n");

        // 1. REGISTRO DE REPARTIDORES (Mínimo 6 repartidores requerido)
        Repartidor[] repartidores = {
            new Repartidor("REP-101", "Carlos Gómez", "Armenia", true),
            new Repartidor("REP-205", "Laura Martínez", "Salento", true),
            new Repartidor("REP-309", "Andrés Quintero", "Calarcá", true),
            new Repartidor("REP-412", "Diana Loaiza", "Circasia", true),
            new Repartidor("REP-518", "Mateo Cardona", "Filandia", true),
            new Repartidor("REP-624", "Valentina Ríos", "Montenegro", true),
            new Repartidor("REP-730", "Jorge Arias", "Armenia", true)
        };

        for (Repartidor r : repartidores) {
            centro.registrarRepartidor(r);
        }
        System.out.println("-> Repartidores registrados exitosamente: " + repartidores.length);

        // 2. REGISTRO DE 30 PAQUETES (Códigos no consecutivos, 6 destinos, diferentes pesos y prioridades)
        Paquete[] datosIniciales = {
            new Paquete("PQ731", "Armenia", 4.5, 5, 45),
            new Paquete("PQ105", "Salento", 3.0, 5, 25),
            new Paquete("PQ942", "Armenia", 7.2, 4, 20),
            new Paquete("PQ318", "Calarcá", 12.0, 3, 30),
            new Paquete("PQ567", "Salento", 2.3, 2, 40),
            new Paquete("PQ829", "Armenia", 6.8, 4, 35),
            new Paquete("PQ420", "Circasia", 1.5, 3, 30),
            new Paquete("PQ214", "Filandia", 8.4, 5, 25),
            new Paquete("PQ653", "Montenegro", 5.1, 1, 50),
            new Paquete("PQ190", "Calarcá", 9.6, 4, 15),
            new Paquete("PQ884", "Armenia", 3.2, 2, 22),
            new Paquete("PQ512", "Salento", 18.7, 5, 60),
            new Paquete("PQ376", "Circasia", 4.0, 3, 28),
            new Paquete("PQ901", "Filandia", 2.9, 1, 45),
            new Paquete("PQ147", "Montenegro", 11.3, 4, 35),
            new Paquete("PQ765", "Armenia", 5.5, 3, 18),
            new Paquete("PQ298", "Calarcá", 7.4, 2, 27),
            new Paquete("PQ619", "Salento", 1.8, 5, 25),
            new Paquete("PQ483", "Circasia", 6.2, 4, 32),
            new Paquete("PQ850", "Filandia", 10.1, 2, 48),
            new Paquete("PQ112", "Montenegro", 3.7, 3, 29),
            new Paquete("PQ975", "Armenia", 14.2, 5, 55),
            new Paquete("PQ341", "Calarcá", 2.1, 1, 20),
            new Paquete("PQ708", "Salento", 8.9, 4, 38),
            new Paquete("PQ259", "Circasia", 5.0, 2, 24),
            new Paquete("PQ690", "Filandia", 13.5, 5, 42),
            new Paquete("PQ437", "Montenegro", 4.8, 3, 31),
            new Paquete("PQ816", "Armenia", 9.0, 4, 26),
            new Paquete("PQ539", "Calarcá", 6.4, 5, 19),
            new Paquete("PQ174", "Salento", 2.6, 1, 33)
        };

        for (Paquete p : datosIniciales) {
            centro.registrarPaquete(p);
        }
        System.out.println("-> Paquetes registrados en orden de llegada: " + centro.getPaquetesOrdenLlegada().size());
        System.out.println("-> Prueba rechazo de código duplicado (PQ731): "
                + centro.registrarPaquete(new Paquete("PQ731", "Armenia", 1.0, 1, 10)));

        // 3. CONSULTA RÁPIDA O(1) POR CÓDIGO
        System.out.println("\n--- CONSULTA RÁPIDA DE PAQUETE POR CÓDIGO ---");
        System.out.println("Consulta PQ512: " + centro.consultarPaquetePorCodigo("PQ512"));
        System.out.println("Consulta PQ999 (Inexistente): " + centro.consultarPaquetePorCodigo("PQ999"));

        // 4. MUNICIPIOS SIN DUPLICADOS Y ORDENADOS ALFABÉTICAMENTE
        System.out.println("\n--- GESTIÓN DE MUNICIPIOS DESTINO ---");
        System.out.println("Municipios con entregas (HashSet): " + centro.obtenerMunicipiosConEntregas());
        System.out.println("Municipios ordenados alfabéticamente (TreeSet): " + centro.obtenerMunicipiosOrdenados());

        // 5. PAQUETES AGRUPADOS POR MUNICIPIO
        System.out.println("\n--- PAQUETES DESTINADOS A ARMENIA ---");
        for (Paquete p : centro.obtenerPaquetesPorMunicipio("Armenia")) {
            System.out.println("  * " + p);
        }

        // 6. ATENCIÓN POR ORDEN DE LLEGADA Y POR PRIORIDAD + HISTORIAL PROPIO
        System.out.println("\n--- DESPACHO DE PAQUETES Y REGISTRO EN LISTA PROPIA ---");
        Paquete atendidoLlegada = centro.atenderSiguientePorOrdenLlegada();
        System.out.println("1° Despachado por ORDEN DE LLEGADA: " + atendidoLlegada);

        Paquete atendidoPrioridad1 = centro.atenderSiguientePorPrioridad();
        System.out.println("2° Despachado por PRIORIDAD: " + atendidoPrioridad1);

        Paquete atendidoPrioridad2 = centro.atenderSiguientePorPrioridad();
        System.out.println("3° Despachado por PRIORIDAD: " + atendidoPrioridad2);

        System.out.println("\n--- HISTORIAL DE ENTREGAS (Recorrido con Iterable / Iterator propio) ---");
        System.out.println("Total entregados en lista simplemente enlazada: " + centro.getHistorialEntregas().tamano());
        for (Paquete entregado : centro.getHistorialEntregas()) {
            System.out.println("  [Entregado] -> " + entregado);
        }

        // 7. ORDENAMIENTOS CON COMPARABLE Y COMPARATOR (Primeros 5 de cada reporte)
        System.out.println("\n--- REPORTES ORDENADOS (Top 5) ---");
        mostrarTop5("Orden Natural por Código (Comparable)", centro.obtenerPaquetesOrdenadosPorCodigo());
        mostrarTop5("Por Prioridad Descendente (Comparator)", centro.obtenerPaquetesOrdenadosPorPrioridad());
        mostrarTop5("Por Peso Descendente (Comparator)", centro.obtenerPaquetesOrdenadosPorPeso());
        mostrarTop5("Por Tiempo Estimado Ascendente (Comparator)", centro.obtenerPaquetesOrdenadosPorTiempo());

        // 8. ALGORITMOS RECURSIVOS, BÚSQUEDA BINARIA Y DIVIDE Y VENCERÁS
        System.out.println("\n--- ANÁLISIS ALGORÍTMICO (Integrante 2) ---");
        double pesoArmenia = centro.calcularPesoTotalPorMunicipio("Armenia");
        System.out.printf("1. [Recursivo] Peso total hacia Armenia: %.2f kg%n", pesoArmenia);

        int cantPrioridadAlta = centro.contarPaquetesConPrioridadMinima(4);
        System.out.println("2. [Recursivo] Cantidad de paquetes con prioridad >= 4: " + cantPrioridadAlta);

        List<Paquete> ordenadosCodigo = centro.obtenerPaquetesOrdenadosPorCodigo();
        Paquete encontradoBinaria = centro.buscarPaqueteBinariaPorCodigo(ordenadosCodigo, "PQ731");
        System.out.println("3. [Búsqueda Binaria] Búsqueda de 'PQ731': " + encontradoBinaria);

        Paquete masPesado = centro.obtenerPaqueteMayorPesoDivideYVenceras();
        System.out.println("4. [Divide y Vencerás] Paquete de mayor peso: " + masPesado);
    }

    private static void mostrarTop5(String titulo, List<Paquete> lista) {
        System.out.println("* " + titulo + ":");
        for (int i = 0; i < Math.min(5, lista.size()); i++) {
            System.out.println("    " + (i + 1) + ". " + lista.get(i));
        }
    }
}