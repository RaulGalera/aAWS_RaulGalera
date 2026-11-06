package es.florida.biblioteca.modelo;

import java.time.LocalDate;

public record Socio(int id, String nombre, String email, LocalDate fechaAlta, int prestamosAbiertos) {
}
