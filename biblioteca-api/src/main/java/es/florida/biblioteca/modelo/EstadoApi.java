package es.florida.biblioteca.modelo;

/** Respuesta de GET /api/estado. */
public record EstadoApi(
        String estado,
        String alumno,
        int maximoPrestamos,
        int diasPrestamo,
        String versionMysql,
        long memoriaUsadaMiB,
        long memoriaMaximaMiB) {
}
