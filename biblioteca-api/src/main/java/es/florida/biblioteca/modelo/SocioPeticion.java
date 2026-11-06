package es.florida.biblioteca.modelo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** La fecha de alta no se acepta: la asigna el servidor. */
public record SocioPeticion(
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 120, message = "el nombre no puede superar 120 caracteres")
        String nombre,

        @NotBlank(message = "el email es obligatorio")
        @Email(message = "el email no tiene un formato válido")
        @Size(max = 160, message = "el email no puede superar 160 caracteres")
        String email) {
}
