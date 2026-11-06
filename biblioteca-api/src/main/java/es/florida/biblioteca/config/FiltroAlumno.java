package es.florida.biblioteca.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Añade la cabecera X-Alumno a TODAS las respuestas, también a las de error.
 * Se pone antes de pasar la petición al resto de la cadena, así ya está
 * en la respuesta aunque el controlador lance una excepción.
 */
@Component
public class FiltroAlumno extends OncePerRequestFilter {

    private final BibliotecaProperties propiedades;

    public FiltroAlumno(BibliotecaProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        respuesta.setHeader("X-Alumno", propiedades.alumno());
        cadena.doFilter(peticion, respuesta);
    }
}
