package es.florida.biblioteca.dao;

import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.modelo.LibroPeticion;

import java.util.List;
import java.util.Optional;

public interface LibroDao {

    /** Búsqueda con filtros opcionales (null = sin filtro); el título por coincidencia parcial. */
    List<Libro> buscar(String titulo, String genero, Integer autorId);

    Optional<Libro> buscarPorId(int id);

    boolean existe(int id);

    /** ¿Hay otro libro (distinto de excluirId, que puede ser null) con este ISBN? */
    boolean existeIsbn(String isbn, Integer excluirId);

    int insertar(LibroPeticion datos);

    boolean actualizar(int id, LibroPeticion datos);

    boolean borrar(int id);

    /** Préstamos registrados del libro, en cualquier estado. */
    int contarPrestamos(int id);

    /**
     * Bloquea la fila del libro hasta el final de la transacción (SELECT ... FOR UPDATE)
     * y devuelve sus ejemplares; vacío si el libro no existe.
     */
    Optional<Integer> bloquearYLeerEjemplares(int id);
}
