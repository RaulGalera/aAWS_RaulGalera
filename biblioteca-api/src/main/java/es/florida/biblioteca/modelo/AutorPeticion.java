package es.florida.biblioteca.modelo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST/PUT /api/autores. El id nunca se acepta: lo genera la BD. */
public record AutorPeticion(
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 120, message = "el nombre no puede superar 120 caracteres")
        String nombre,

        @Size(max = 60, message = "la nacionalidad no puede superar 60 caracteres")
        String nacionalidad,

        @Max(value = 2100, message = "el año de nacimiento no es válido")
        Integer anioNacimiento) {
}
