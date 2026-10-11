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
        if (destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException("El destino del paquete no puede ser nulo ni vacío.");
        }
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso del paquete debe ser mayor que cero.");
        }
        if (prioridad < 1 || prioridad > 5) {
            throw new IllegalArgumentException("La prioridad debe estar comprendida entre 1 y 5.");
        }
        if (tiempoEstimado <= 0) {
            throw new IllegalArgumentException("El tiempo estimado debe ser un entero positivo mayor que cero.");
        }

        this.codigo = codigo.trim();
        this.destino = destino.trim();
        this.peso = peso;
        this.prioridad = prioridad;
        this.tiempoEstimado = tiempoEstimado;
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

    /**
     * Orden natural del paquete: por código en orden alfabético ascendente.
     */
    @Override
    public int compareTo(Paquete otro) {
        if (otro == null) {
            throw new NullPointerException("No se puede comparar con un paquete nulo.");
        }
        return this.codigo.compareTo(otro.codigo);
    }

    /**
     * Dos paquetes son iguales si tienen el mismo código único.
     */
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