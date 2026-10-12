package modelo;

import java.text.Normalizer;

/**
 * Utilidades de texto compartidas por el modelo y el servicio.
 * Permiten que "Calarca", "calarcá" y "CALARCÁ" se reconozcan como el mismo municipio.
 */
public final class Texto {

    // Constructor privado para evitar que la clase utilitaria sea instanciada
    private Texto() {
    }

    /**
     * Clave de comparación: minúsculas, sin tildes y con un solo espacio entre palabras.
     * Ej: "  Calarcá " -> "calarca"
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().replaceAll("\\s+", " ");
    }

    /**
     * Formatea un nombre propio: primera letra de cada palabra en mayúscula.
     * Ej: "la tebaida" -> "La Tebaida"
     */
    public static String formatearNombrePropio(String texto) {
        String[] palabras = texto.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < palabras.length; i++) {
            if (!palabras[i].isEmpty()) {
                sb.append(Character.toUpperCase(palabras[i].charAt(0)))
                  .append(palabras[i].substring(1));
                if (i < palabras.length - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }
}
