package es.florida.biblioteca.dao;

import es.florida.biblioteca.modelo.EstadoPrestamo;
import es.florida.biblioteca.modelo.Prestamo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrestamoDao {

    /** estado null = todos. */
    List<Prestamo> buscar(EstadoPrestamo estado);

    Optional<Prestamo> buscarPorId(int id);

    List<Prestamo> buscarPorSocio(int socioId);

    /** Informe de la variante 3: préstamos del libro, del más reciente al más antiguo. */
    List<Prestamo> buscarPorLibro(int libroId);

    int contarVencidosDeSocio(int socioId);

    int contarAbiertosDeSocio(int socioId);

    int contarAbiertosDeLibro(int libroId);

    boolean tieneAbiertoElLibro(int socioId, int libroId);

    int insertar(int libroId, int socioId, LocalDate fechaPrestamo, LocalDate fechaLimite);

    /** Pone fecha_devolucion solo si estaba abierto. @return true si se ha registrado */
    boolean registrarDevolucion(int id, LocalDate fecha);
}
