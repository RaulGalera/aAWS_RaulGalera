package es.florida.biblioteca.modelo;

/**
 * ACTIVO: abierto (sin devolución) y dentro de plazo.
 * VENCIDO: abierto y con fecha_limite anterior a hoy.
 * DEVUELTO: tiene fecha_devolucion.
 */
public enum EstadoPrestamo {
    ACTIVO, VENCIDO, DEVUELTO
}
