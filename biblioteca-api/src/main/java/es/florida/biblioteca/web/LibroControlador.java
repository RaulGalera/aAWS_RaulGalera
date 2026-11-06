package es.florida.biblioteca.web;

import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.modelo.LibroPeticion;
import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.servicio.LibroServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroControlador {

    private final LibroServicio servicio;

    public LibroControlador(LibroServicio servicio) {
        this.servicio = servicio;
    }

    /** GET /api/libros?titulo=&genero=&autorId=  (los tres opcionales y combinables) */
    @GetMapping
    public List<Libro> buscar(@RequestParam(required = false) String titulo,
                              @RequestParam(required = false) String genero,
                              @RequestParam(required = false) Integer autorId) {
        return servicio.buscar(titulo, genero, autorId);
    }

    @GetMapping("/{id}")
    public Libro obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    /** Informe de la variante 3: préstamos del libro, del más reciente al más antiguo. */
    @GetMapping("/{id}/historial")
    public List<Prestamo> historial(@PathVariable int id) {
        return servicio.historial(id);
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@Valid @RequestBody LibroPeticion datos) {
        Libro creado = servicio.crear(datos);
        return ResponseEntity.created(AutorControlador.ubicacion(creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public Libro modificar(@PathVariable int id, @Valid @RequestBody LibroPeticion datos) {
        return servicio.modificar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable int id) {
        servicio.borrar(id);
        return ResponseEntity.noContent().build();
    }
}
