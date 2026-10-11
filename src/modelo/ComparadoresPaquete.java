package modelo;

import java.util.Comparator;

public final class ComparadoresPaquete {

    // Constructor privado para evitar que la clase utilitaria sea instanciada
    private ComparadoresPaquete() {
    }

    /**
     * Regla de despacho prioritario:
     * 1. Mayor prioridad primero (orden descendente).
     * 2. Menor tiempo estimado de entrega (orden ascendente).
     * 3. Código en orden alfabético ascendente.
     */
    public static final Comparator<Paquete> DESPACHO_PRIORITARIO = new Comparator<Paquete>() {
        @Override
        public int compare(Paquete p1, Paquete p2) {
            // 1. Mayor prioridad primero (comparamos p2 con p1 para orden descendente)
            int cmpPrioridad = Integer.compare(p2.getPrioridad(), p1.getPrioridad());
            if (cmpPrioridad != 0) {
                return cmpPrioridad;
            }
            // 2. Empate en prioridad: menor tiempo estimado primero (p1 con p2 ascendente)
            int cmpTiempo = Integer.compare(p1.getTiempoEstimado(), p2.getTiempoEstimado());
            if (cmpTiempo != 0) {
                return cmpTiempo;
            }
            // 3. Empate en anteriores: código alfabéticamente ascendente
            return p1.getCodigo().compareTo(p2.getCodigo());
        }
    };

    /**
     * Ordena por prioridad de mayor a menor.
     */
    public static final Comparator<Paquete> POR_PRIORIDAD_DESC = new Comparator<Paquete>() {
        @Override
        public int compare(Paquete p1, Paquete p2) {
            return Integer.compare(p2.getPrioridad(), p1.getPrioridad());
        }
    };

    /**
     * Ordena por peso de mayor a menor.
     */
    public static final Comparator<Paquete> POR_PESO_DESC = new Comparator<Paquete>() {
        @Override
        public int compare(Paquete p1, Paquete p2) {
            return Double.compare(p2.getPeso(), p1.getPeso());
        }
    };

    /**
     * Ordena por tiempo estimado de menor a mayor.
     */
    public static final Comparator<Paquete> POR_TIEMPO_ASC = new Comparator<Paquete>() {
        @Override
        public int compare(Paquete p1, Paquete p2) {
            return Integer.compare(p1.getTiempoEstimado(), p2.getTiempoEstimado());
        }
    };
}