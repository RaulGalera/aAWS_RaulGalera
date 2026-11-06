package es.florida.biblioteca.jdbc;

import es.florida.biblioteca.dao.LibroDao;
import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.modelo.LibroPeticion;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcLibroDao implements LibroDao {

    /** disponibles = ejemplares - préstamos abiertos (se calcula, no se guarda). */
    private static final String SELECT_BASE =
            "SELECT l.id, l.isbn, l.titulo, l.anio_publicacion, l.genero, l.ejemplares, "
            + "       l.autor_id, a.nombre AS autor_nombre, "
            + "       l.ejemplares - (SELECT COUNT(*) FROM prestamo p "
            + "                       WHERE p.libro_id = l.id AND p.fecha_devolucion IS NULL) AS disponibles "
            + "FROM libro l JOIN autor a ON a.id = l.autor_id";

    private static final RowMapper<Libro> MAPEADOR = (rs, numFila) -> new Libro(
            rs.getInt("id"),
            rs.getString("isbn"),
            rs.getString("titulo"),
            rs.getObject("anio_publicacion", Integer.class),
            rs.getString("genero"),
            rs.getInt("ejemplares"),
            rs.getInt("disponibles"),
            rs.getInt("autor_id"),
            rs.getString("autor_nombre"));

    private final JdbcClient jdbc;

    public JdbcLibroDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Libro> buscar(String titulo, String genero, Integer autorId) {
        // Las condiciones se añaden según los filtros recibidos, pero los VALORES
        // siempre viajan como parámetros con nombre: nunca se concatenan al SQL.
        List<String> condiciones = new ArrayList<>();
        Map<String, Object> parametros = new HashMap<>();
        if (titulo != null && !titulo.isBlank()) {
            condiciones.add("LOWER(l.titulo) LIKE LOWER(:titulo)");
            parametros.put("titulo", "%" + titulo.trim() + "%");
        }
        if (genero != null && !genero.isBlank()) {
            condiciones.add("LOWER(l.genero) = LOWER(:genero)");
            parametros.put("genero", genero.trim());
        }
        if (autorId != null) {
            condiciones.add("l.autor_id = :autorId");
            parametros.put("autorId", autorId);
        }
        String sql = SELECT_BASE
                + (condiciones.isEmpty() ? "" : " WHERE " + String.join(" AND ", condiciones))
                + " ORDER BY l.titulo";
        return jdbc.sql(sql).params(parametros).query(MAPEADOR).list();
    }

    @Override
    public Optional<Libro> buscarPorId(int id) {
        return jdbc.sql(SELECT_BASE + " WHERE l.id = :id")
                .param("id", id)
                .query(MAPEADOR)
                .optional();
    }

    @Override
    public boolean existe(int id) {
        return jdbc.sql("SELECT COUNT(*) FROM libro WHERE id = :id")
                .param("id", id)
                .query(Integer.class)
                .single() > 0;
    }

    @Override
    public boolean existeIsbn(String isbn, Integer excluirId) {
        return jdbc.sql("SELECT COUNT(*) FROM libro WHERE isbn = :isbn AND (:excluir IS NULL OR id <> :excluir)")
                .param("isbn", isbn)
                .param("excluir", excluirId)
                .query(Integer.class)
                .single() > 0;
    }

    @Override
    public int insertar(LibroPeticion datos) {
        KeyHolder clave = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO libro (isbn, titulo, anio_publicacion, genero, ejemplares, autor_id) "
                        + "VALUES (:isbn, :titulo, :anio, :genero, :ejemplares, :autorId)")
                .param("isbn", datos.isbn())
                .param("titulo", datos.titulo())
                .param("anio", datos.anioPublicacion())
                .param("genero", datos.genero())
                .param("ejemplares", datos.ejemplares())
                .param("autorId", datos.autorId())
                .update(clave, "id");
        return clave.getKey().intValue();
    }

    @Override
    public boolean actualizar(int id, LibroPeticion datos) {
        return jdbc.sql("UPDATE libro SET isbn = :isbn, titulo = :titulo, anio_publicacion = :anio, "
                        + "genero = :genero, ejemplares = :ejemplares, autor_id = :autorId WHERE id = :id")
                .param("isbn", datos.isbn())
                .param("titulo", datos.titulo())
                .param("anio", datos.anioPublicacion())
                .param("genero", datos.genero())
                .param("ejemplares", datos.ejemplares())
                .param("autorId", datos.autorId())
                .param("id", id)
                .update() > 0;
    }

    @Override
    public boolean borrar(int id) {
        return jdbc.sql("DELETE FROM libro WHERE id = :id")
                .param("id", id)
                .update() > 0;
    }

    @Override
    public int contarPrestamos(int id) {
        return jdbc.sql("SELECT COUNT(*) FROM prestamo WHERE libro_id = :id")
                .param("id", id)
                .query(Integer.class)
                .single();
    }

    @Override
    public Optional<Integer> bloquearYLeerEjemplares(int id) {
        // FOR UPDATE: otra transacción que quiera bloquear este libro espera a nuestro commit
        return jdbc.sql("SELECT ejemplares FROM libro WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query(Integer.class)
                .optional();
    }
}
