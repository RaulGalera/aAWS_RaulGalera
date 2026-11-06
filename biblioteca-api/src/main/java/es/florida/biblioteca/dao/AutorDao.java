package es.florida.biblioteca.dao;

import es.florida.biblioteca.modelo.Autor;
import es.florida.biblioteca.modelo.AutorPeticion;

import java.util.List;
import java.util.Optional;

/** Operaciones sobre autores en términos del dominio, sin nada de SQL ni de JDBC. */
public interface AutorDao {

    List<Autor> buscarTodos();

    Optional<Autor> buscarPorId(int id);

    boolean existe(int id);

    /** @return id generado por la base de datos */
    int insertar(AutorPeticion datos);

    /** @return true si existía y se ha modificado */
    boolean actualizar(int id, AutorPeticion datos);

    /** @return true si existía y se ha borrado */
    boolean borrar(int id);

    int contarLibros(int id);
}
