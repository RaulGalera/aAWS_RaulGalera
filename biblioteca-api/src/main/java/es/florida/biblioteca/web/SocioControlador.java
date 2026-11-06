package es.florida.biblioteca.web;

import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.modelo.Socio;
import es.florida.biblioteca.modelo.SocioPeticion;
import es.florida.biblioteca.servicio.SocioServicio;
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

import java.util.List;

@RestController
@RequestMapping("/api/socios")
public class SocioControlador {

    private final SocioServicio servicio;

    public SocioControlador(SocioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Socio> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Socio obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/prestamos")
    public List<Prestamo> prestamos(@PathVariable int id) {
        return servicio.prestamosDe(id);
    }

    @PostMapping
    public ResponseEntity<Socio> crear(@Valid @RequestBody SocioPeticion datos) {
        Socio creado = servicio.crear(datos);
        return ResponseEntity.created(AutorControlador.ubicacion(creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public Socio modificar(@PathVariable int id, @Valid @RequestBody SocioPeticion datos) {
        return servicio.modificar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable int id) {
        servicio.borrar(id);
        return ResponseEntity.noContent().build();
    }
}
