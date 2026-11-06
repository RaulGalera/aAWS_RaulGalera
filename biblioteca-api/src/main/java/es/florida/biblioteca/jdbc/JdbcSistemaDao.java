package es.florida.biblioteca.jdbc;

import es.florida.biblioteca.dao.SistemaDao;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSistemaDao implements SistemaDao {

    private final JdbcClient jdbc;

    public JdbcSistemaDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String versionBaseDatos() {
        return jdbc.sql("SELECT VERSION()").query(String.class).single();
    }
}
