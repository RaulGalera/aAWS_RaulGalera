package es.florida.biblioteca.modelo;

import java.util.List;

/** Formato común de todos los errores de la API. */
public record ErrorRespuesta(
        String marcaTemporal,
        int estado,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles) {
}
