package es.florida.biblioteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propiedades biblioteca.* de application.properties. Cada una se puede
 * sobrescribir con una variable de entorno de nombre equivalente:
 * biblioteca.prestamo.maximo  ->  BIBLIOTECA_PRESTAMO_MAXIMO.
 */
@ConfigurationProperties(prefix = "biblioteca")
public record BibliotecaProperties(
        @DefaultValue("sin-asignar") String alumno,
        @DefaultValue Prestamo prestamo) {

    public record Prestamo(
            @DefaultValue("3") int maximo,
            @DefaultValue("14") int dias) {
    }
}
