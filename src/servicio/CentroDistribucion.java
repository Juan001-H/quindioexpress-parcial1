package servicio;

import estructuras.ColeccionSimple;
import estructuras.ListaSimplementeEnlazada;
import modelo.ComparadoresPaquete;
import modelo.Paquete;
import modelo.Repartidor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class CentroDistribucion {

    // 1. Conserva el orden exacto de registro para auditorías y análisis
    private final List<Paquete> paquetesOrdenLlegada;

    // 2. Búsqueda rápida O(1) promedio de paquetes por su código único
    private final Map<String, Paquete> paquetesPorCodigo;

    // 3. Municipios con al menos un paquete registrado (sin duplicados)
    private final Set<String> municipiosDestino;

    // 4. Municipios ordenados alfabéticamente y sin duplicados
    private final Set<String> municipiosOrdenados;

    // 5. Agrupación de paquetes según su municipio de destino
    private final Map<String, List<Paquete>> paquetesPorMunicipio;

    // 6. Cola FIFO para atención estricta por orden de llegada
    private final Queue<Paquete> colaOrdenLlegada;

    // 7. Cola de prioridad según reglas de despacho prioritario
    private final PriorityQueue<Paquete> colaPrioridad;

    // 8. Registro de repartidores por identificación única
    private final Map<String, Repartidor> repartidoresPorId;

    // 9. Historial de paquetes entregados usando la lista simplemente enlazada propia
    private final ColeccionSimple<Paquete> historialEntregas;

    public CentroDistribucion() {
        this.paquetesOrdenLlegada = new ArrayList<>();
        this.paquetesPorCodigo = new HashMap<>();
        this.municipiosDestino = new HashSet<>();
        this.municipiosOrdenados = new TreeSet<>();
        this.paquetesPorMunicipio = new HashMap<>();
        this.colaOrdenLlegada = new ArrayDeque<>();
        this.colaPrioridad = new PriorityQueue<>(ComparadoresPaquete.DESPACHO_PRIORITARIO);
        this.repartidoresPorId = new HashMap<>();
        this.historialEntregas = new ListaSimplementeEnlazada<>();
    }

    // =========================================================================
    // SECCIÓN INTEGRANTE 1: COLECCIONES, CONSULTAS, DESPACHO Y ORDENAMIENTO
    // =========================================================================

    /**
     * Registra un nuevo paquete verificando que su código no esté previamente registrado.
     */
    public boolean registrarPaquete(Paquete p) {
        if (p == null || paquetesPorCodigo.containsKey(p.getCodigo())) {
            return false;
        }

        paquetesOrdenLlegada.add(p);
        paquetesPorCodigo.put(p.getCodigo(), p);
        municipiosDestino.add(p.getDestino());
        municipiosOrdenados.add(p.getDestino());

        List<Paquete> listaMunicipio = paquetesPorMunicipio.get(p.getDestino());
        if (listaMunicipio == null) {
            listaMunicipio = new ArrayList<>();
            paquetesPorMunicipio.put(p.getDestino(), listaMunicipio);
        }
        listaMunicipio.add(p);

        colaOrdenLlegada.offer(p);
        colaPrioridad.offer(p);

        return true;
    }

    /**
     * Registra un nuevo repartidor validando que su identificación sea única.
     */
    public boolean registrarRepartidor(Repartidor r) {
        if (r == null || repartidoresPorId.containsKey(r.getIdentificacion())) {
            return false;
        }
        repartidoresPorId.put(r.getIdentificacion(), r);
        return true;
    }

    /**
     * Consulta un paquete por su código en tiempo constante O(1) promedio.
     */
    public Paquete consultarPaquetePorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        return paquetesPorCodigo.get(codigo.trim());
    }

    /**
     * Consulta un repartidor por su identificación en O(1) promedio.
     */
    public Repartidor consultarRepartidorPorId(String identificacion) {
        if (identificacion == null) {
            return null;
        }
        return repartidoresPorId.get(identificacion.trim());
    }

    /**
     * Retorna el conjunto de municipios con entregas registradas (sin duplicados).
     */
    public Set<String> obtenerMunicipiosConEntregas() {
        return new HashSet<>(municipiosDestino);
    }

    /**
     * Retorna el conjunto de municipios ordenados alfabéticamente y sin duplicados.
     */
    public Set<String> obtenerMunicipiosOrdenados() {
        return new TreeSet<>(municipiosOrdenados);
    }

    /**
     * Retorna la lista de paquetes destinados a un municipio específico.
     */
    public List<Paquete> obtenerPaquetesPorMunicipio(String municipio) {
        if (municipio == null) {
            return Collections.emptyList();
        }
        List<Paquete> paquetes = paquetesPorMunicipio.get(municipio.trim());
        if (paquetes == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(paquetes);
    }

    /**
     * Busca un repartidor disponible cuya zona coincida con el municipio destino.
     */
    public Repartidor buscarRepartidorDisponibleParaZona(String destino) {
        if (destino == null) {
            return null;
        }
        for (Repartidor r : repartidoresPorId.values()) {
            if (r.isDisponible() && r.getZona().equalsIgnoreCase(destino.trim())) {
                return r;
            }
        }
        return null;
    }

    /**
     * Atiende el siguiente paquete respetando estrictamente el orden de llegada (FIFO),
     * validando que el repartidor esté disponible y pertenezca a la zona del paquete.
     */
    public Paquete atenderSiguientePorOrdenLlegada(Repartidor repartidor) {
        if (repartidor == null || !repartidor.isDisponible() || colaOrdenLlegada.isEmpty()) {
            return null;
        }

        Paquete siguiente = colaOrdenLlegada.peek();
        if (!repartidor.getZona().equalsIgnoreCase(siguiente.getDestino())) {
            return null;
        }

        colaOrdenLlegada.poll();
        colaPrioridad.remove(siguiente);

        repartidor.setDisponible(false);
        historialEntregas.agregar(siguiente);

        return siguiente;
    }

    /**
     * Sobrecarga: Atiende el siguiente paquete por orden de llegada buscando
     * automáticamente un repartidor disponible para ese destino.
     */
    public Paquete atenderSiguientePorOrdenLlegada() {
        if (colaOrdenLlegada.isEmpty()) {
            return null;
        }
        Paquete siguiente = colaOrdenLlegada.peek();
        Repartidor repartidor = buscarRepartidorDisponibleParaZona(siguiente.getDestino());
        if (repartidor == null) {
            return null;
        }
        return atenderSiguientePorOrdenLlegada(repartidor);
    }

    /**
     * Atiende el siguiente paquete según las reglas de despacho prioritario
     * (1° Prioridad desc, 2° Tiempo asc, 3° Código asc), validando al repartidor.
     */
    public Paquete atenderSiguientePorPrioridad(Repartidor repartidor) {
        if (repartidor == null || !repartidor.isDisponible() || colaPrioridad.isEmpty()) {
            return null;
        }

        Paquete prioritario = colaPrioridad.peek();
        if (!repartidor.getZona().equalsIgnoreCase(prioritario.getDestino())) {
            return null;
        }

        colaPrioridad.poll();
        colaOrdenLlegada.remove(prioritario);

        repartidor.setDisponible(false);
        historialEntregas.agregar(prioritario);

        return prioritario;
    }

    /**
     * Sobrecarga: Atiende el siguiente paquete por prioridad buscando
     * automáticamente un repartidor disponible para ese destino.
     */
    public Paquete atenderSiguientePorPrioridad() {
        if (colaPrioridad.isEmpty()) {
            return null;
        }
        Paquete prioritario = colaPrioridad.peek();
        Repartidor repartidor = buscarRepartidorDisponibleParaZona(prioritario.getDestino());
        if (repartidor == null) {
            return null;
        }
        return atenderSiguientePorPrioridad(repartidor);
    }

    /**
     * Retorna una copia de los paquetes ordenada por prioridad (de mayor a menor).
     */
    public List<Paquete> obtenerPaquetesOrdenadosPorPrioridad() {
        List<Paquete> copia = new ArrayList<>(paquetesOrdenLlegada);
        copia.sort(ComparadoresPaquete.POR_PRIORIDAD_DESC);
        return copia;
    }

    /**
     * Retorna una copia de los paquetes ordenada por peso (de mayor a menor).
     */
    public List<Paquete> obtenerPaquetesOrdenadosPorPeso() {
        List<Paquete> copia = new ArrayList<>(paquetesOrdenLlegada);
        copia.sort(ComparadoresPaquete.POR_PESO_DESC);
        return copia;
    }

    /**
     * Retorna una copia de los paquetes ordenada por tiempo estimado (de menor a mayor).
     */
    public List<Paquete> obtenerPaquetesOrdenadosPorTiempo() {
        List<Paquete> copia = new ArrayList<>(paquetesOrdenLlegada);
        copia.sort(ComparadoresPaquete.POR_TIEMPO_ASC);
        return copia;
    }

    /**
     * Retorna una copia de los paquetes ordenada por su orden natural (código ascendente).
     */
    public List<Paquete> obtenerPaquetesOrdenadosPorCodigo() {
        List<Paquete> copia = new ArrayList<>(paquetesOrdenLlegada);
        Collections.sort(copia);
        return copia;
    }

    public List<Paquete> getPaquetesOrdenLlegada() {
        return paquetesOrdenLlegada;
    }

    public ColeccionSimple<Paquete> getHistorialEntregas() {
        return historialEntregas;
    }

    public Map<String, Repartidor> getRepartidoresPorId() {
        return repartidoresPorId;
    }

    public int obtenerCantidadPendientes() {
        return colaOrdenLlegada.size();
    }

    // =========================================================================
    // SECCIÓN INTEGRANTE 2: RECURSIVIDAD, BÚSQUEDA BINARIA Y DIVIDE Y VENCERÁS
    // =========================================================================

    /**
     * 1. RECURSIVIDAD: Calcula el peso total de los paquetes destinados a un municipio.
     */
    public double calcularPesoTotalPorMunicipio(String municipio) {
        if (municipio == null || municipio.trim().isEmpty()) {
            return 0.0;
        }
        return calcularPesoTotalRecursivo(paquetesOrdenLlegada, municipio.trim(), 0);
    }

    private double calcularPesoTotalRecursivo(List<Paquete> lista, String municipio, int indice) {
        // Caso base: se recorrió toda la lista
        if (indice >= lista.size()) {
            return 0.0;
        }

        Paquete actual = lista.get(indice);
        double pesoActual = actual.getDestino().equalsIgnoreCase(municipio) ? actual.getPeso() : 0.0;

        // Caso recursivo: avanza hacia el caso base incrementando (indice + 1)
        return pesoActual + calcularPesoTotalRecursivo(lista, municipio, indice + 1);
    }

    /**
     * 2. RECURSIVIDAD: Cuenta cuántos paquetes tienen prioridad >= prioridadMinima.
     */
    public int contarPaquetesConPrioridadMinima(int prioridadMinima) {
        return contarPrioridadRecursivo(paquetesOrdenLlegada, prioridadMinima, 0);
    }

    private int contarPrioridadRecursivo(List<Paquete> lista, int prioridadMinima, int indice) {
        // Caso base: se llegó al final de la lista
        if (indice >= lista.size()) {
            return 0;
        }

        int sumaActual = (lista.get(indice).getPrioridad() >= prioridadMinima) ? 1 : 0;

        // Caso recursivo: avanza hacia el caso base con (indice + 1)
        return sumaActual + contarPrioridadRecursivo(lista, prioridadMinima, indice + 1);
    }

    /**
     * 3. BÚSQUEDA BINARIA: Busca un paquete por código sobre una lista previamente ordenada por código.
     */
    public Paquete buscarPaqueteBinariaPorCodigo(List<Paquete> listaOrdenadaPorCodigo, String codigoBuscado) {
        if (listaOrdenadaPorCodigo == null || listaOrdenadaPorCodigo.isEmpty() || codigoBuscado == null) {
            return null;
        }
        return busquedaBinariaRecursiva(listaOrdenadaPorCodigo, codigoBuscado.trim(), 0, listaOrdenadaPorCodigo.size() - 1);
    }

    private Paquete busquedaBinariaRecursiva(List<Paquete> lista, String codigoBuscado, int inicio, int fin) {
        // Caso base 1: el rango se cruzó, el paquete no existe
        if (inicio > fin) {
            return null;
        }

        int medio = inicio + (fin - inicio) / 2;
        Paquete paqueteMedio = lista.get(medio);
        int comparacion = paqueteMedio.getCodigo().compareTo(codigoBuscado);

        // Caso base 2: se encontró el paquete en la posición media
        if (comparacion == 0) {
            return paqueteMedio;
        }

        // Caso recursivo: buscar en la mitad izquierda o derecha
        if (comparacion > 0) {
            return busquedaBinariaRecursiva(lista, codigoBuscado, inicio, medio - 1);
        } else {
            return busquedaBinariaRecursiva(lista, codigoBuscado, medio + 1, fin);
        }
    }

    /**
     * 4. DIVIDE Y VENCERÁS: Determina el paquete de mayor peso dentro del conjunto.
     */
    public Paquete obtenerPaqueteMayorPesoDivideYVenceras() {
        if (paquetesOrdenLlegada.isEmpty()) {
            return null;
        }
        return mayorPesoDivideYVenceras(paquetesOrdenLlegada, 0, paquetesOrdenLlegada.size() - 1);
    }

    private Paquete mayorPesoDivideYVenceras(List<Paquete> lista, int inicio, int fin) {
        // 1. Caso base: el subproblema tiene un único elemento
        if (inicio == fin) {
            return lista.get(inicio);
        }

        // 2. División del problema: punto medio
        int medio = inicio + (fin - inicio) / 2;

        // 3. Subproblema izquierdo
        Paquete maxIzquierdo = mayorPesoDivideYVenceras(lista, inicio, medio);

        // 4. Subproblema derecho
        Paquete maxDerecho = mayorPesoDivideYVenceras(lista, medio + 1, fin);

        // 5. Combinación de resultados
        if (maxIzquierdo.getPeso() >= maxDerecho.getPeso()) {
            return maxIzquierdo;
        } else {
            return maxDerecho;
        }
    }
}
