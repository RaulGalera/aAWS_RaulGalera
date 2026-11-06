package es.florida.biblioteca.jdbc;

import es.florida.biblioteca.dao.AutorDao;
import es.florida.biblioteca.modelo.Autor;
import es.florida.biblioteca.modelo.AutorPeticion;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Implementación de referencia del enunciado: RowMapper explícito y parámetros con nombre. */
@Repository
public class JdbcAutorDao implements AutorDao {

    private static final String SELECT_BASE =
            "SELECT id, nombre, nacionalidad, anio_nacimiento FROM autor";

    private static final RowMapper<Autor> MAPEADOR = (rs, numFila) -> new Autor(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("nacionalidad"),
            rs.getObject("anio_nacimiento", Integer.class)); // admite NULL

    private final JdbcClient jdbc;

    public JdbcAutorDao(JdbcClient jdbc) { // inyección por constructor
        this.jdbc = jdbc;
    }

    @Override
    public List<Autor> buscarTodos() {
        return jdbc.sql(SELECT_BASE + " ORDER BY nombre")
                .query(MAPEADOR)
                .list();
    }

    @Override
    public Optional<Autor> buscarPorId(int id) {
        return jdbc.sql(SELECT_BASE + " WHERE id = :id")
                .param("id", id)
                .query(MAPEADOR)
                .optional();
    }

    @Override
    public boolean existe(int id) {
        return jdbc.sql("SELECT COUNT(*) FROM autor WHERE id = :id")
                .param("id", id)
                .query(Integer.class)
                .single() > 0;
    }

    @Override
    public int insertar(AutorPeticion datos) {
        KeyHolder clave = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO autor (nombre, nacionalidad, anio_nacimiento) "
                        + "VALUES (:nombre, :nacionalidad, :anio)")
                .param("nombre", datos.nombre())
                .param("nacionalidad", datos.nacionalidad())
                .param("anio", datos.anioNacimiento())
                .update(clave, "id"); // indicar la columna: necesario en PostgreSQL
        return clave.getKey().intValue(); // id generado por la base de datos
    }

    @Override
    public boolean actualizar(int id, AutorPeticion datos) {
        return jdbc.sql("UPDATE autor SET nombre = :nombre, nacionalidad = :nacionalidad, "
                        + "anio_nacimiento = :anio WHERE id = :id")
                .param("nombre", datos.nombre())
                .param("nacionalidad", datos.nacionalidad())
                .param("anio", datos.anioNacimiento())
                .param("id", id)
                .update() > 0;
    }

    @Override
    public boolean borrar(int id) {
        return jdbc.sql("DELETE FROM autor WHERE id = :id")
                .param("id", id)
                .update() > 0;
    }

    @Override
    public int contarLibros(int id) {
        return jdbc.sql("SELECT COUNT(*) FROM libro WHERE autor_id = :id")
                .param("id", id)
                .query(Integer.class)
                .single();
    }
}
