package es.florida.biblioteca.servicio;

import es.florida.biblioteca.dao.AutorDao;
import es.florida.biblioteca.dao.LibroDao;
import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.Autor;
import es.florida.biblioteca.modelo.AutorPeticion;
import es.florida.biblioteca.modelo.Libro;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reglas de negocio de autores. Solo conoce las INTERFACES de los DAO. */
@Service
public class AutorServicio {

    private final AutorDao autorDao;
    private final LibroDao libroDao;

    public AutorServicio(AutorDao autorDao, LibroDao libroDao) {
        this.autorDao = autorDao;
        this.libroDao = libroDao;
    }

    public List<Autor> listar() {
        return autorDao.buscarTodos();
    }

    public Autor obtener(int id) {
        return autorDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el autor " + id));
    }

    public List<Libro> librosDe(int id) {
        if (!autorDao.existe(id)) {
            throw new RecursoNoEncontradoException("No existe el autor " + id);
        }
        return libroDao.buscar(null, null, id);
    }

    @Transactional
    public Autor crear(AutorPeticion datos) {
        int id = autorDao.insertar(datos);
        return obtener(id);
    }

    @Transactional
    public Autor modificar(int id, AutorPeticion datos) {
        if (!autorDao.actualizar(id, datos)) {
            throw new RecursoNoEncontradoException("No existe el autor " + id);
        }
        return obtener(id);
    }

    @Transactional
    public void borrar(int id) {
        if (!autorDao.existe(id)) {
            throw new RecursoNoEncontradoException("No existe el autor " + id);
        }
        if (autorDao.contarLibros(id) > 0) {
            throw new ConflictoException("El autor " + id + " tiene libros y no se puede borrar");
        }
        autorDao.borrar(id);
    }
}
