package es.florida.biblioteca.modelo;

import java.time.LocalDate;

public record Prestamo(
        int id,
        int libroId,
        String libroTitulo,
        int socioId,
        String socioNombre,
        LocalDate fechaPrestamo,
        LocalDate fechaLimite,
        LocalDate fechaDevolucion,
        EstadoPrestamo estado) {
}
