package modelo;

import java.util.Comparator;

public class ComparadoresPaquete {

    /**
     * Criterio compuesto para la PriorityQueue de Despacho Prioritario:
     * 1. Mayor prioridad primero (b vs a).
     * 2. Empate: Menor tiempo estimado primero (a vs b).
     * 3. Empate: Código alfabéticamente ascendente (a vs b).
     */
    public static final Comparator<Paquete> DESPACHO_PRIORITARIO = (a, b) -> {
        // 1. Mayor prioridad primero (orden descendente)
        int cmpPrioridad = Integer.compare(b.getPrioridad(), a.getPrioridad());
        if (cmpPrioridad != 0) {
            return cmpPrioridad;
        }
        // 2. Menor tiempo estimado primero (orden ascendente)
        int cmpTiempo = Integer.compare(a.getTiempoEstimado(), b.getTiempoEstimado());
        if (cmpTiempo != 0) {
            return cmpTiempo;
        }
        // 3. Código en orden alfabético ascendente
        return a.getCodigo().compareTo(b.getCodigo());
    };

    /**
     * Ordena por prioridad de MAYOR a MENOR.
     */
    public static final Comparator<Paquete> POR_PRIORIDAD_DESC = (a, b) ->
            Integer.compare(b.getPrioridad(), a.getPrioridad());

    /**
     * Ordena por peso de MAYOR a MENOR.
     */
    public static final Comparator<Paquete> POR_PESO_DESC = (a, b) ->
            Double.compare(b.getPeso(), a.getPeso());

    /**
     * Ordena por tiempo estimado de MENOR a MAYOR.
     */
    public static final Comparator<Paquete> POR_TIEMPO_ASC = (a, b) ->
            Integer.compare(a.getTiempoEstimado(), b.getTiempoEstimado());
}