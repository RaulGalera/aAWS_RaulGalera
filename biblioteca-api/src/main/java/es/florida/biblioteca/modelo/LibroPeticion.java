package es.florida.biblioteca.modelo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LibroPeticion(
        @NotBlank(message = "el ISBN es obligatorio")
        @Pattern(regexp = "\\d{13}", message = "el ISBN debe tener exactamente 13 dígitos")
        String isbn,

        @NotBlank(message = "el título es obligatorio")
        @Size(max = 200, message = "el título no puede superar 200 caracteres")
        String titulo,

        @Max(value = 2100, message = "el año de publicación no es válido")
        Integer anioPublicacion,

        @NotBlank(message = "el género es obligatorio")
        @Size(max = 40, message = "el género no puede superar 40 caracteres")
        String genero,

        @NotNull(message = "el número de ejemplares es obligatorio")
        @Min(value = 0, message = "los ejemplares no pueden ser negativos")
        @Max(value = 1000, message = "como máximo 1000 ejemplares")
        Integer ejemplares,

        @NotNull(message = "el autor es obligatorio")
        Integer autorId) {
}
