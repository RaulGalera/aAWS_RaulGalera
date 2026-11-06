package es.florida.biblioteca.excepcion;

/** El error está en los datos que envía el cliente (cuerpo o parámetros) -> 400. */
public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
