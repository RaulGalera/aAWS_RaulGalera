package es.florida.biblioteca.dao;

/** Consultas de diagnóstico de la base de datos (para /api/estado). */
public interface SistemaDao {

    /** SELECT VERSION(); lanza DataAccessException si MySQL no responde. */
    String versionBaseDatos();
}
