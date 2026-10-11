/**
 * Contrato común genérico exigido por QuindíoExpress para sus estructuras propias.
 * Extiende Iterable<T> para permitir recorrer la estructura sin exponer sus nodos.
 */
public interface ColeccionSimple<T> extends Iterable<T> {
    void agregar(T elemento);
    boolean eliminar(T elemento);
    T obtener(int indice);
    int tamano();
    boolean estaVacia();
}