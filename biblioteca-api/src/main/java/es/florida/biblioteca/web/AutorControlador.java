package es.florida.biblioteca.web;

import es.florida.biblioteca.modelo.Autor;
import es.florida.biblioteca.modelo.AutorPeticion;
import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.servicio.AutorServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/autores")
public class AutorControlador {

    private final AutorServicio servicio;

    public AutorControlador(AutorServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Autor> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Autor obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/libros")
    public List<Libro> libros(@PathVariable int id) {
        return servicio.librosDe(id);
    }

    @PostMapping
    public ResponseEntity<Autor> crear(@Valid @RequestBody AutorPeticion datos) {
        Autor creado = servicio.crear(datos);
        return ResponseEntity.created(ubicacion(creado.id())).body(creado); // 201 + Location
    }

    @PutMapping("/{id}")
    public Autor modificar(@PathVariable int id, @Valid @RequestBody AutorPeticion datos) {
        return servicio.modificar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable int id) {
        servicio.borrar(id);
        return ResponseEntity.noContent().build(); // 204
    }

    static URI ubicacion(int id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
