package modelo;

import java.util.Objects;

public class Paquete implements Comparable<Paquete> {

    private String codigo;
    private String destino;
    private double peso;
    private int prioridad;
    private int tiempoEstimado;

    public Paquete(String codigo, String destino, double peso, int prioridad, int tiempoEstimado) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del paquete no puede ser nulo ni vacío.");
        }
        String codLimpio = codigo.trim().toUpperCase();
        // Debe contener al menos una letra y un número (ej. PQ731). Rechaza solo números como "564"
        if (!codLimpio.matches("^(?=.*[\\p{L}])(?=.*[0-9])[\\p{L}0-9-]+$")) {
            throw new IllegalArgumentException("El código '" + codigo + "' es inválido. Debe ser alfanumérico (letras y números, ej. PQ731).");
        }

        if (destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException("El municipio destino no puede ser nulo ni vacío.");
        }
        String destLimpio = destino.trim();
        // Solo permite letras y espacios (rechaza números como "65" o símbolos)
        if (!destLimpio.matches("^[\\p{L}\\s]+$")) {
            throw new IllegalArgumentException("El destino '" + destino + "' es inválido. Solo debe contener letras (no números).");
        }

        if (Double.isNaN(peso) || Double.isInfinite(peso) || peso <= 0) {
            throw new IllegalArgumentException("El peso del paquete debe ser un número mayor que cero.");
        }
        if (prioridad < 1 || prioridad > 5) {
            throw new IllegalArgumentException("La prioridad debe estar comprendida entre 1 y 5.");
        }
        if (tiempoEstimado <= 0) {
            throw new IllegalArgumentException("El tiempo estimado debe ser un entero positivo mayor que cero.");
        }

        this.codigo = codLimpio;
        this.destino = formatearNombrePropio(destLimpio);
        this.peso = peso;
        this.prioridad = prioridad;
        this.tiempoEstimado = tiempoEstimado;
    }

    private static String formatearNombrePropio(String texto) {
        String[] palabras = texto.toLowerCase().split("\\s+");
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

    public String getCodigo() {
        return codigo;
    }

    public String getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public int getTiempoEstimado() {
        return tiempoEstimado;
    }

    @Override
    public int compareTo(Paquete otro) {
        if (otro == null) {
            throw new NullPointerException("No se puede comparar con un paquete nulo.");
        }
        return this.codigo.compareTo(otro.codigo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Paquete otro = (Paquete) obj;
        return Objects.equals(this.codigo, otro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.codigo);
    }

    @Override
    public String toString() {
        return String.format("Paquete[codigo=%s, destino=%s, peso=%.2f kg, prioridad=%d, tiempo=%d min]",
                codigo, destino, peso, prioridad, tiempoEstimado);
    }
}
