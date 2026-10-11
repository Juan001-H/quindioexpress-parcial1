

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ListaSimplementeEnlazada<T> implements ColeccionSimple<T> {

    // Clase interna privada: el exterior NUNCA conoce ni toca los nodos directamente
    private static class Nodo<T> {
        T valor;
        Nodo<T> siguiente;

        Nodo(T valor) {
            this.valor = valor;
            this.siguiente = null;
        }
    }

    private Nodo<T> cabeza; // Primer elemento
    private Nodo<T> cola;   // Último elemento (optimiza inserción al final a O(1))
    private int tamano;     // Cantidad de elementos almacenados

    public ListaSimplementeEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    /**
     * Agrega un elemento al final de la lista (conserva el orden cronológico de entregas).
     * Complejidad: O(1) gracias al puntero 'cola'.
     */
    @Override
    public void agregar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("No se permiten elementos nulos en la lista.");
        }
        Nodo<T> nuevo = new Nodo<>(elemento);

        // Caso 1: La lista está vacía
        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            // Caso 2: Ya hay elementos, lo enganchamos después de la cola actual
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamano++;
    }

    /**
     * Elimina la primera aparición del elemento y retorna true si lo eliminó, o false si no existe.
     * Cubre los 6 casos exigidos en el documento.
     * Complejidad: O(n) porque toca buscar el nodo.
     */
    @Override
    public boolean eliminar(T elemento) {
        // Caso 1: Lista vacía o elemento nulo
        if (estaVacia() || elemento == null) {
            return false;
        }

        // Caso 2 y 3: El elemento a eliminar está en la CABEZA (primer elemento)
        if (Objects.equals(cabeza.valor, elemento)) {
            cabeza = cabeza.siguiente;
            tamano--;
            // Si había un único elemento, la lista quedó vacía y debemos actualizar 'cola' a null
            if (cabeza == null) {
                cola = null;
            }
            return true;
        }

        // Buscamos en el resto de la lista manteniendo referencia al nodo 'anterior'
        Nodo<T> anterior = cabeza;
        Nodo<T> actual = cabeza.siguiente;

        while (actual != null) {
            if (Objects.equals(actual.valor, elemento)) {
                // Saltamos el nodo 'actual' desconectándolo de la cadena
                anterior.siguiente = actual.siguiente;

                // Caso 5: Si el nodo eliminado era el ÚLTIMO ('cola'), actualizamos 'cola' al anterior
                if (actual == cola) {
                    cola = anterior;
                }
                tamano--;
                return true; // Caso 4 (intermedio) o Caso 5 (último) resuelto con éxito
            }
            anterior = actual;
            actual = actual.siguiente;
        }

        // Caso 6: Se recorrió toda la lista y el elemento no existe
        return false;
    }

    /**
     * Obtiene el elemento en una posición específica (de 0 a tamano - 1).
     * Complejidad: O(n) porque debemos avanzar nodo por nodo hasta el índice.
     */
    @Override
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.valor;
    }

    @Override
    public int tamano() {
        return tamano;
    }

    @Override
    public boolean estaVacia() {
        return tamano == 0; // También equivale a: cabeza == null
    }

    /**
     * Proporciona el mecanismo Iterator<T> para recorrer la lista uno a uno
     * sin exponer los nodos internos al exterior.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No hay más elementos en la lista.");
                }
                T dato = actual.valor;
                actual = actual.siguiente; // Avanza al siguiente nodo internamente
                return dato;
            }
        };
    }
}