package es.florida.biblioteca.web;

import es.florida.biblioteca.excepcion.DatosInvalidosException;
import es.florida.biblioteca.modelo.EstadoPrestamo;
import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.modelo.PrestamoPeticion;
import es.florida.biblioteca.servicio.PrestamoServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoControlador {

    private final PrestamoServicio servicio;

    public PrestamoControlador(PrestamoServicio servicio) {
        this.servicio = servicio;
    }

    /** GET /api/prestamos?estado=ACTIVO|VENCIDO|DEVUELTO (opcional; otro valor -> 400) */
    @GetMapping
    public List<Prestamo> listar(@RequestParam(required = false) String estado) {
        EstadoPrestamo filtro = null;
        if (estado != null && !estado.isBlank()) {
            try {
                filtro = EstadoPrestamo.valueOf(estado.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new DatosInvalidosException("Estado no válido: " + estado
                        + " (valores admitidos: ACTIVO, VENCIDO, DEVUELTO)");
            }
        }
        return servicio.listar(filtro);
    }

    @GetMapping("/{id}")
    public Prestamo obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Prestamo> crear(@Valid @RequestBody PrestamoPeticion peticion) {
        Prestamo creado = servicio.crear(peticion);
        return ResponseEntity.created(AutorControlador.ubicacion(creado.id())).body(creado);
    }

    @PutMapping("/{id}/devolucion")
    public Prestamo devolver(@PathVariable int id) {
        return servicio.devolver(id);
    }
}
