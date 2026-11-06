package es.florida.biblioteca.modelo;

/** disponibles no se guarda en la BD: es ejemplares - préstamos abiertos. */
public record Libro(
        int id,
        String isbn,
        String titulo,
        Integer anioPublicacion,
        String genero,
        int ejemplares,
        int disponibles,
        int autorId,
        String autorNombre) {
}
