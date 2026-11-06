package es.florida.biblioteca.servicio;

import es.florida.biblioteca.config.BibliotecaProperties;
import es.florida.biblioteca.dao.LibroDao;
import es.florida.biblioteca.dao.PrestamoDao;
import es.florida.biblioteca.dao.SocioDao;
import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.DatosInvalidosException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.EstadoPrestamo;
import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.modelo.PrestamoPeticion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoServicio {

    private final PrestamoDao prestamoDao;
    private final LibroDao libroDao;
    private final SocioDao socioDao;
    private final BibliotecaProperties propiedades;

    public PrestamoServicio(PrestamoDao prestamoDao, LibroDao libroDao, SocioDao socioDao,
                            BibliotecaProperties propiedades) {
        this.prestamoDao = prestamoDao;
        this.libroDao = libroDao;
        this.socioDao = socioDao;
        this.propiedades = propiedades;
    }

    public List<Prestamo> listar(EstadoPrestamo estado) {
        return prestamoDao.buscar(estado);
    }

    public Prestamo obtener(int id) {
        return prestamoDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el préstamo " + id));
    }

    /**
     * Alta de un préstamo en UNA transacción: o se hace todo o no se hace nada.
     *
     * Concurrencia: las comprobaciones se hacen sobre filas bloqueadas con SELECT ... FOR UPDATE.
     * Si quedan 1 ejemplar y llegan dos peticiones a la vez, la segunda espera en el bloqueo
     * del libro hasta que la primera hace commit, y entonces ve 0 disponibles (409).
     * Se bloquea SIEMPRE primero el libro y después el socio, para evitar interbloqueos.
     */
    @Transactional
    public Prestamo crear(PrestamoPeticion peticion) {
        int libroId = peticion.libroId();
        int socioId = peticion.socioId();
        int maximo = propiedades.prestamo().maximo();

        // 1. El libro y el socio existen (400: el error está en el cuerpo). Bloqueo: libro -> socio
        int ejemplares = libroDao.bloquearYLeerEjemplares(libroId)   // SELECT ... FOR UPDATE (libro)
                .orElseThrow(() -> new DatosInvalidosException("No existe el libro " + libroId));
        if (!socioDao.bloquear(socioId)) {                            // SELECT ... FOR UPDATE (socio)
            throw new DatosInvalidosException("No existe el socio " + socioId);
        }

        // 2. Sin préstamos vencidos
        if (prestamoDao.contarVencidosDeSocio(socioId) > 0) {
            throw new ConflictoException("El socio " + socioId + " tiene préstamos vencidos");
        }
        // 3. Por debajo del máximo de préstamos abiertos de la variante
        if (prestamoDao.contarAbiertosDeSocio(socioId) >= maximo) {
            throw new ConflictoException("El socio " + socioId + " ya tiene el máximo de "
                    + maximo + " préstamos abiertos");
        }
        // 4. No tiene ya abierto un préstamo de ese mismo libro
        if (prestamoDao.tieneAbiertoElLibro(socioId, libroId)) {
            throw new ConflictoException("El socio " + socioId + " ya tiene prestado el libro " + libroId);
        }
        // 5. Queda al menos un ejemplar disponible
        if (ejemplares - prestamoDao.contarAbiertosDeLibro(libroId) <= 0) {
            throw new ConflictoException("No quedan ejemplares disponibles del libro " + libroId);
        }

        LocalDate hoy = LocalDate.now();
        int id = prestamoDao.insertar(libroId, socioId, hoy, hoy.plusDays(propiedades.prestamo().dias()));
        return obtener(id);
    }

    @Transactional
    public Prestamo devolver(int id) {
        Prestamo prestamo = obtener(id);
        if (prestamo.fechaDevolucion() != null) {
            throw new ConflictoException("El préstamo " + id + " ya estaba devuelto");
        }
        if (!prestamoDao.registrarDevolucion(id, LocalDate.now())) {
            // otra petición lo devolvió entre la lectura y la actualización
            throw new ConflictoException("El préstamo " + id + " ya estaba devuelto");
        }
        return obtener(id);
    }
}
