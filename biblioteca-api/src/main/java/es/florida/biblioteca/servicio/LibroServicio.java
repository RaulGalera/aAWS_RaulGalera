package es.florida.biblioteca.servicio;

import es.florida.biblioteca.dao.AutorDao;
import es.florida.biblioteca.dao.LibroDao;
import es.florida.biblioteca.dao.PrestamoDao;
import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.DatosInvalidosException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.modelo.LibroPeticion;
import es.florida.biblioteca.modelo.Prestamo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibroServicio {

    private final LibroDao libroDao;
    private final AutorDao autorDao;
    private final PrestamoDao prestamoDao;

    public LibroServicio(LibroDao libroDao, AutorDao autorDao, PrestamoDao prestamoDao) {
        this.libroDao = libroDao;
        this.autorDao = autorDao;
        this.prestamoDao = prestamoDao;
    }

    public List<Libro> buscar(String titulo, String genero, Integer autorId) {
        return libroDao.buscar(titulo, genero, autorId);
    }

    public Libro obtener(int id) {
        return libroDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro " + id));
    }

    /**
     * Informe de la variante 3: GET /api/libros/{id}/historial.
     * Primero se comprueba el libro: una lista vacía (libro nunca prestado)
     * no es lo mismo que un libro que no existe (404).
     */
    public List<Prestamo> historial(int id) {
        if (!libroDao.existe(id)) {
            throw new RecursoNoEncontradoException("No existe el libro " + id);
        }
        return prestamoDao.buscarPorLibro(id);
    }

    @Transactional
    public Libro crear(LibroPeticion datos) {
        comprobarAutor(datos.autorId());
        if (libroDao.existeIsbn(datos.isbn(), null)) {
            throw new ConflictoException("Ya existe un libro con el ISBN " + datos.isbn());
        }
        int id = libroDao.insertar(datos);
        return obtener(id);
    }

    @Transactional
    public Libro modificar(int id, LibroPeticion datos) {
        // Se bloquea el libro para que no entre un préstamo mientras se cambian los ejemplares
        if (libroDao.bloquearYLeerEjemplares(id).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe el libro " + id);
        }
        comprobarAutor(datos.autorId());
        if (libroDao.existeIsbn(datos.isbn(), id)) {
            throw new ConflictoException("Ya existe otro libro con el ISBN " + datos.isbn());
        }
        int abiertos = prestamoDao.contarAbiertosDeLibro(id);
        if (datos.ejemplares() < abiertos) {
            throw new ConflictoException("El libro " + id + " tiene " + abiertos
                    + " préstamos abiertos: no puede tener menos ejemplares");
        }
        libroDao.actualizar(id, datos);
        return obtener(id);
    }

    @Transactional
    public void borrar(int id) {
        if (!libroDao.existe(id)) {
            throw new RecursoNoEncontradoException("No existe el libro " + id);
        }
        if (libroDao.contarPrestamos(id) > 0) {
            throw new ConflictoException("El libro " + id + " se ha prestado alguna vez y no se puede borrar");
        }
        libroDao.borrar(id);
    }

    /** El autor va en el cuerpo de la petición: si no existe, el error es del cliente (400). */
    private void comprobarAutor(int autorId) {
        if (!autorDao.existe(autorId)) {
            throw new DatosInvalidosException("No existe el autor " + autorId);
        }
    }
}
