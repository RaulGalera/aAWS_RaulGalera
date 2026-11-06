package es.florida.biblioteca.excepcion;

/** La petición es correcta pero choca con una regla de negocio -> 409. */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
