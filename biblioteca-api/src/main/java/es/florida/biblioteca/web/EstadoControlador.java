package es.florida.biblioteca.web;

import es.florida.biblioteca.modelo.EstadoApi;
import es.florida.biblioteca.servicio.EstadoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstadoControlador {

    private final EstadoServicio servicio;

    public EstadoControlador(EstadoServicio servicio) {
        this.servicio = servicio;
    }

    /** Comprobación de salud: 200 si MySQL responde a SELECT VERSION(), 503 si no. */
    @GetMapping("/api/estado")
    public ResponseEntity<EstadoApi> estado() {
        EstadoApi estado = servicio.comprobar();
        HttpStatus codigo = estado.versionMysql() != null ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(codigo).body(estado);
    }
}
