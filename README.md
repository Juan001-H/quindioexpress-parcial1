# QuindíoExpress - Parcial 1 (Estructuras de Datos)

Sistema de gestión de operaciones de un centro de distribución, en Java.

## Integrantes
- Integrante A: Tomas Suaza
- Integrante B: (Nombre de tu compañero)

## Estructura
- `src/modelo`: Clases de dominio (`Paquete`, `Repartidor`) y criterios de ordenamiento (`Comparator`).
- `src/estructuras`: Interfaz genérica y `ListaSimplementeEnlazada` propia con `Iterable`/`Iterator`.
- `src/servicio`: Lógica operativa (`CentroDistribucion`), algoritmos recursivos, búsqueda binaria y divide y vencerás.
- `docs/`: Documento PDF (diseño, pilas de llamadas, bitácora de IA).

## Compilar y ejecutar
```bash
javac -d out $(find src -name "*.java")
java -cp out Main

