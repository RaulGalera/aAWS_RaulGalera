package es.florida.biblioteca.modelo;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de POST /api/prestamos: {"libroId":3,"socioId":1} */
public record PrestamoPeticion(
        @NotNull(message = "el libro es obligatorio") Integer libroId,
        @NotNull(message = "el socio es obligatorio") Integer socioId) {
}
