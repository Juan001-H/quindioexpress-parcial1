package modelo;

import java.util.Objects;

public class Repartidor {

    private String identificacion;
    private String nombre;
    private String zona;
    private boolean disponible;

    public Repartidor(String identificacion, String nombre, String zona, boolean disponible) {
        if (identificacion == null || identificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La identificación del repartidor no puede ser nula ni vacía.");
        }
        String idLimpio = identificacion.trim().toUpperCase();
        if (!idLimpio.matches("^[\\p{L}0-9-]+$")) {
            throw new IllegalArgumentException("La identificación solo puede contener letras, números o guiones (ej. REP-101).");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del repartidor no puede ser nulo ni vacío.");
        }
        if (!nombre.trim().matches("^[\\p{L}\\s]+$")) {
            throw new IllegalArgumentException("El nombre '" + nombre + "' es inválido. Solo debe contener letras, no números.");
        }

        if (zona == null || zona.trim().isEmpty()) {
            throw new IllegalArgumentException("La zona del repartidor no puede ser nula ni vacía.");
        }
        if (!zona.trim().matches("^[\\p{L}\\s]+$")) {
            throw new IllegalArgumentException("La zona '" + zona + "' es inválida. Solo debe contener letras, no números.");
        }

        this.identificacion = idLimpio;
        this.nombre = nombre.trim();
        this.zona = zona.trim();
        this.disponible = disponible;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getZona() {
        return zona;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Repartidor otro = (Repartidor) obj;
        return Objects.equals(this.identificacion, otro.identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identificacion);
    }

    @Override
    public String toString() {
        return String.format("Repartidor[id=%s, nombre=%s, zona=%s, disponible=%s]",
                identificacion, nombre, zona, disponible ? "Sí" : "No");
    }
}

