package es.florida.biblioteca.jdbc;

import es.florida.biblioteca.dao.PrestamoDao;
import es.florida.biblioteca.modelo.EstadoPrestamo;
import es.florida.biblioteca.modelo.Prestamo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPrestamoDao implements PrestamoDao {

    /** El estado se calcula con las mismas definiciones que 04_verificacion.sql. */
    private static final String SELECT_BASE =
            "SELECT p.id, p.libro_id, l.titulo AS libro_titulo, p.socio_id, s.nombre AS socio_nombre, "
            + "       p.fecha_prestamo, p.fecha_limite, p.fecha_devolucion, "
            + "       CASE WHEN p.fecha_devolucion IS NOT NULL THEN 'DEVUELTO' "
            + "            WHEN p.fecha_limite < CURRENT_DATE   THEN 'VENCIDO' "
            + "            ELSE 'ACTIVO' END AS estado "
            + "FROM prestamo p "
            + "JOIN libro l ON l.id = p.libro_id "
            + "JOIN socio s ON s.id = p.socio_id";

    /** Condición SQL fija para cada estado (elegida por el enum, nunca por texto del cliente). */
    private static final String ABIERTO = "p.fecha_devolucion IS NULL";
    private static final String VENCIDO = ABIERTO + " AND p.fecha_limite < CURRENT_DATE";

    private static final RowMapper<Prestamo> MAPEADOR = (rs, numFila) -> new Prestamo(
            rs.getInt("id"),
            rs.getInt("libro_id"),
            rs.getString("libro_titulo"),
            rs.getInt("socio_id"),
            rs.getString("socio_nombre"),
            rs.getObject("fecha_prestamo", LocalDate.class),
            rs.getObject("fecha_limite", LocalDate.class),
            rs.getObject("fecha_devolucion", LocalDate.class), // NULL si está abierto
            EstadoPrestamo.valueOf(rs.getString("estado")));

    private final JdbcClient jdbc;

    public JdbcPrestamoDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Prestamo> buscar(EstadoPrestamo estado) {
        String condicion = switch (estado) {
            case null -> null;
            case DEVUELTO -> "p.fecha_devolucion IS NOT NULL";
            case VENCIDO -> VENCIDO;
            case ACTIVO -> ABIERTO + " AND p.fecha_limite >= CURRENT_DATE";
        };
        String sql = SELECT_BASE + (condicion == null ? "" : " WHERE " + condicion) + " ORDER BY p.id";
        return jdbc.sql(sql).query(MAPEADOR).list();
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) {
        return jdbc.sql(SELECT_BASE + " WHERE p.id = :id")
                .param("id", id)
                .query(MAPEADOR)
                .optional();
    }

    @Override
    public List<Prestamo> buscarPorSocio(int socioId) {
        return jdbc.sql(SELECT_BASE + " WHERE p.socio_id = :socioId ORDER BY p.fecha_prestamo DESC, p.id DESC")
                .param("socioId", socioId)
                .query(MAPEADOR)
                .list();
    }

    @Override
    public List<Prestamo> buscarPorLibro(int libroId) {
        // Variante 3: del más reciente al más antiguo; a igual fecha, el último registrado primero
        return jdbc.sql(SELECT_BASE + " WHERE p.libro_id = :libroId ORDER BY p.fecha_prestamo DESC, p.id DESC")
                .param("libroId", libroId)
                .query(MAPEADOR)
                .list();
    }

    @Override
    public int contarVencidosDeSocio(int socioId) {
        return contar("SELECT COUNT(*) FROM prestamo p WHERE p.socio_id = :id AND " + VENCIDO, socioId);
    }

    @Override
    public int contarAbiertosDeSocio(int socioId) {
        return contar("SELECT COUNT(*) FROM prestamo p WHERE p.socio_id = :id AND " + ABIERTO, socioId);
    }

    @Override
    public int contarAbiertosDeLibro(int libroId) {
        return contar("SELECT COUNT(*) FROM prestamo p WHERE p.libro_id = :id AND " + ABIERTO, libroId);
    }

    @Override
    public boolean tieneAbiertoElLibro(int socioId, int libroId) {
        return jdbc.sql("SELECT COUNT(*) FROM prestamo p WHERE p.socio_id = :socioId "
                        + "AND p.libro_id = :libroId AND " + ABIERTO)
                .param("socioId", socioId)
                .param("libroId", libroId)
                .query(Integer.class)
                .single() > 0;
    }

    @Override
    public int insertar(int libroId, int socioId, LocalDate fechaPrestamo, LocalDate fechaLimite) {
        KeyHolder clave = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO prestamo (libro_id, socio_id, fecha_prestamo, fecha_limite) "
                        + "VALUES (:libroId, :socioId, :fecha, :limite)")
                .param("libroId", libroId)
                .param("socioId", socioId)
                .param("fecha", fechaPrestamo)
                .param("limite", fechaLimite)
                .update(clave, "id");
        return clave.getKey().intValue();
    }

    @Override
    public boolean registrarDevolucion(int id, LocalDate fecha) {
        return jdbc.sql("UPDATE prestamo SET fecha_devolucion = :fecha WHERE id = :id AND fecha_devolucion IS NULL")
                .param("fecha", fecha)
                .param("id", id)
                .update() > 0;
    }

    private int contar(String sql, int id) {
        return jdbc.sql(sql).param("id", id).query(Integer.class).single();
    }
}
