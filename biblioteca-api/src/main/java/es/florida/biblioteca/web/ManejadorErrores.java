package es.florida.biblioteca.web;

import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.DatosInvalidosException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Única clase que convierte TODAS las excepciones en el JSON de error común.
 * Así los controladores no tienen try/catch: solo lanzan excepciones.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException e, HttpServletRequest pet) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage(), pet, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> rutaInexistente(NoResourceFoundException e, HttpServletRequest pet) {
        return respuesta(HttpStatus.NOT_FOUND, "La ruta no existe", pet, List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorRespuesta> conflicto(ConflictoException e, HttpServletRequest pet) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage(), pet, List.of());
    }

    /** Red de seguridad: si dos peticiones a la vez saltan una comprobación, la restricción UNIQUE/FK de MySQL lo frena. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> integridad(DataIntegrityViolationException e, HttpServletRequest pet) {
        return respuesta(HttpStatus.CONFLICT, "La operación viola una restricción de la base de datos", pet, List.of());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(DatosInvalidosException e, HttpServletRequest pet) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage(), pet, List.of());
    }

    /** Fallos de @Valid: una entrada en detalles por cada campo incorrecto. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException e, HttpServletRequest pet) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", pet, detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonIlegible(HttpMessageNotReadableException e, HttpServletRequest pet) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido", pet, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> tipoIncorrecto(MethodArgumentTypeMismatchException e, HttpServletRequest pet) {
        return respuesta(HttpStatus.BAD_REQUEST, "El parámetro '" + e.getName() + "' no tiene un valor válido", pet, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> metodo(HttpRequestMethodNotSupportedException e, HttpServletRequest pet) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método " + e.getMethod() + " no permitido en esta ruta", pet, List.of());
    }

    /**
     * Base de datos caída o inaccesible -> 503, no 500: el cliente no ha hecho nada mal,
     * es el servidor el que no puede atenderle ahora. CannotCreateTransactionException
     * es lo que llega cuando un método @Transactional no consigue conexión.
     */
    @ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    public ResponseEntity<ErrorRespuesta> baseDatosNoDisponible(Exception e, HttpServletRequest pet) {
        LOG.error("Base de datos no disponible: {}", e.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "La base de datos no está disponible en este momento", pet, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception e, HttpServletRequest pet) {
        LOG.error("Error no controlado en {}", pet.getRequestURI(), e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", pet, List.of());
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus estado, String mensaje,
                                                     HttpServletRequest pet, List<String> detalles) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(
                OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                pet.getRequestURI(),
                detalles);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
