package es.florida.biblioteca.dao;

import es.florida.biblioteca.modelo.Socio;
import es.florida.biblioteca.modelo.SocioPeticion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SocioDao {

    List<Socio> buscarTodos();

    Optional<Socio> buscarPorId(int id);

    boolean existe(int id);

    boolean existeEmail(String email, Integer excluirId);

    int insertar(SocioPeticion datos, LocalDate fechaAlta);

    boolean actualizar(int id, SocioPeticion datos);

    boolean borrar(int id);

    int contarPrestamos(int id);

    /** Bloquea la fila del socio hasta el final de la transacción; false si no existe. */
    boolean bloquear(int id);
}
