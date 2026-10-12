package servicio;

import estructuras.ColeccionSimple;
import estructuras.ListaSimplementeEnlazada;
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
import modelo.ComparadoresPaquete;
import modelo.Paquete;
import modelo.Repartidor;

public class CentroDistribucion {

    private final List<Paquete> paquetesOrdenLlegada;
    private final Map<String, Paquete> paquetesPorCodigo;
    private final Set<String> municipiosDestino;
    private final Set<String> municipiosOrdenados;
    private final Map<String, List<Paquete>> paquetesPorMunicipio;
    private final Queue<Paquete> colaOrdenLlegada;
    private final PriorityQueue<Paquete> colaPrioridad;
    private final Map<String, Repartidor> repartidoresPorId;
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

    public boolean registrarPaquete(Paquete p) {
        if (p == null) {
            throw new IllegalArgumentException("El paquete a registrar no puede ser nulo.");
        }
        String codigoClave = p.getCodigo().trim().toUpperCase();
        if (paquetesPorCodigo.containsKey(codigoClave)) {
            return false; // Rechaza duplicados en O(1)
        }

        paquetesOrdenLlegada.add(p);
        paquetesPorCodigo.put(codigoClave, p);
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

    public boolean registrarRepartidor(Repartidor r) {
        if (r == null) {
            throw new IllegalArgumentException("El repartidor no puede ser nulo.");
        }
        String idClave = r.getIdentificacion().trim().toUpperCase();
        if (repartidoresPorId.containsKey(idClave)) {
            return false;
        }
        repartidoresPorId.put(idClave, r);
        return true;
    }

    public Paquete consultarPaquetePorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        return paquetesPorCodigo.get(codigo.trim().toUpperCase());
    }

    public Repartidor consultarRepartidorPorId(String identificacion) {
        if (identificacion == null || identificacion.trim().isEmpty()) {
            return null;
        }
        return repartidoresPorId.get(identificacion.trim().toUpperCase());
    }

    public Set<String> obtenerMunicipiosConEntregas() {
        return new HashSet<>(municipiosDestino);
    }

    public Set<String> obtenerMunicipiosOrdenados() {
        return new TreeSet<>(municipiosOrdenados);
    }

    public List<Paquete> obtenerPaquetesPorMunicipio(String municipio) {
        if (municipio == null || municipio.trim().isEmpty()) {
            return Collections.emptyList();
        }
        for (Map.Entry<String, List<Paquete>> entry : paquetesPorMunicipio.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(municipio.trim())) {
                return new ArrayList<>(entry.getValue());
            }
        }
        return Collections.emptyList();
    }

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

    public Paquete verSiguienteEnColaLlegada() {
        return colaOrdenLlegada.peek();
    }

    public Paquete verSiguienteEnColaPrioridad() {
        return colaPrioridad.peek();
    }

    public List<Paquete> obtenerPaquetesOrdenadosPorPrioridad() {
        List<Paquete> copia = new ArrayList<>(paquetesOrdenLlegada);
        copia.sort(ComparadoresPaquete.POR_PRIORIDAD_DESC);
        return copia;
    }

    public List<Paquete> obtenerPaquetesOrdenadosPorPeso() {
        List<Paquete> copia = new ArrayList<>(paquetesOrdenLlegada);
        copia.sort(ComparadoresPaquete.POR_PESO_DESC);
        return copia;
    }

    public List<Paquete> obtenerPaquetesOrdenadosPorTiempo() {
        List<Paquete> copia = new ArrayList<>(paquetesOrdenLlegada);
        copia.sort(ComparadoresPaquete.POR_TIEMPO_ASC);
        return copia;
    }

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

    public double calcularPesoTotalPorMunicipio(String municipio) {
        if (municipio == null || municipio.trim().isEmpty()) {
            return 0.0;
        }
        return calcularPesoTotalRecursivo(paquetesOrdenLlegada, municipio.trim(), 0);
    }

    private double calcularPesoTotalRecursivo(List<Paquete> lista, String municipio, int indice) {
        if (indice >= lista.size()) {
            return 0.0;
        }
        Paquete actual = lista.get(indice);
        double pesoActual = actual.getDestino().equalsIgnoreCase(municipio) ? actual.getPeso() : 0.0;
        return pesoActual + calcularPesoTotalRecursivo(lista, municipio, indice + 1);
    }

    public int contarPaquetesConPrioridadMinima(int prioridadMinima) {
        return contarPrioridadRecursivo(paquetesOrdenLlegada, prioridadMinima, 0);
    }

    private int contarPrioridadRecursivo(List<Paquete> lista, int prioridadMinima, int indice) {
        if (indice >= lista.size()) {
            return 0;
        }
        int sumaActual = (lista.get(indice).getPrioridad() >= prioridadMinima) ? 1 : 0;
        return sumaActual + contarPrioridadRecursivo(lista, prioridadMinima, indice + 1);
    }

    public Paquete buscarPaqueteBinariaPorCodigo(List<Paquete> listaOrdenadaPorCodigo, String codigoBuscado) {
        if (listaOrdenadaPorCodigo == null || listaOrdenadaPorCodigo.isEmpty() || codigoBuscado == null) {
            return null;
        }
        return busquedaBinariaRecursiva(listaOrdenadaPorCodigo, codigoBuscado.trim().toUpperCase(), 0, listaOrdenadaPorCodigo.size() - 1);
    }

    private Paquete busquedaBinariaRecursiva(List<Paquete> lista, String codigoBuscado, int inicio, int fin) {
        if (inicio > fin) {
            return null;
        }
        int medio = inicio + (fin - inicio) / 2;
        Paquete paqueteMedio = lista.get(medio);
        int comparacion = paqueteMedio.getCodigo().compareTo(codigoBuscado);

        if (comparacion == 0) {
            return paqueteMedio;
        }
        if (comparacion > 0) {
            return busquedaBinariaRecursiva(lista, codigoBuscado, inicio, medio - 1);
        } else {
            return busquedaBinariaRecursiva(lista, codigoBuscado, medio + 1, fin);
        }
    }

    public Paquete obtenerPaqueteMayorPesoDivideYVenceras() {
        if (paquetesOrdenLlegada.isEmpty()) {
            return null;
        }
        return mayorPesoDivideYVenceras(paquetesOrdenLlegada, 0, paquetesOrdenLlegada.size() - 1);
    }

    private Paquete mayorPesoDivideYVenceras(List<Paquete> lista, int inicio, int fin) {
        if (inicio == fin) {
            return lista.get(inicio);
        }
        int medio = inicio + (fin - inicio) / 2;
        Paquete maxIzquierdo = mayorPesoDivideYVenceras(lista, inicio, medio);
        Paquete maxDerecho = mayorPesoDivideYVenceras(lista, medio + 1, fin);

        if (maxIzquierdo.getPeso() >= maxDerecho.getPeso()) {
            return maxIzquierdo;
        } else {
            return maxDerecho;
        }
    }
}
