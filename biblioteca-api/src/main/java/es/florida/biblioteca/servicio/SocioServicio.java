package es.florida.biblioteca.servicio;

import es.florida.biblioteca.dao.PrestamoDao;
import es.florida.biblioteca.dao.SocioDao;
import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.modelo.Socio;
import es.florida.biblioteca.modelo.SocioPeticion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SocioServicio {

    private final SocioDao socioDao;
    private final PrestamoDao prestamoDao;

    public SocioServicio(SocioDao socioDao, PrestamoDao prestamoDao) {
        this.socioDao = socioDao;
        this.prestamoDao = prestamoDao;
    }

    public List<Socio> listar() {
        return socioDao.buscarTodos();
    }

    public Socio obtener(int id) {
        return socioDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el socio " + id));
    }

    public List<Prestamo> prestamosDe(int id) {
        if (!socioDao.existe(id)) {
            throw new RecursoNoEncontradoException("No existe el socio " + id);
        }
        return prestamoDao.buscarPorSocio(id);
    }

    @Transactional
    public Socio crear(SocioPeticion datos) {
        if (socioDao.existeEmail(datos.email(), null)) {
            throw new ConflictoException("Ya existe un socio con el email " + datos.email());
        }
        int id = socioDao.insertar(datos, LocalDate.now()); // la fecha de alta la pone el servidor
        return obtener(id);
    }

    @Transactional
    public Socio modificar(int id, SocioPeticion datos) {
        if (!socioDao.existe(id)) {
            throw new RecursoNoEncontradoException("No existe el socio " + id);
        }
        if (socioDao.existeEmail(datos.email(), id)) {
            throw new ConflictoException("Ya existe otro socio con el email " + datos.email());
        }
        socioDao.actualizar(id, datos);
        return obtener(id);
    }

    @Transactional
    public void borrar(int id) {
        if (!socioDao.existe(id)) {
            throw new RecursoNoEncontradoException("No existe el socio " + id);
        }
        if (socioDao.contarPrestamos(id) > 0) {
            throw new ConflictoException("El socio " + id + " tiene préstamos registrados y no se puede borrar");
        }
        socioDao.borrar(id);
    }
}
