package es.florida.biblioteca.servicio;

import es.florida.biblioteca.config.BibliotecaProperties;
import es.florida.biblioteca.dao.SistemaDao;
import es.florida.biblioteca.modelo.EstadoApi;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class EstadoServicio {

    private static final long MIB = 1024 * 1024;

    private final SistemaDao sistemaDao;
    private final BibliotecaProperties propiedades;

    public EstadoServicio(SistemaDao sistemaDao, BibliotecaProperties propiedades) {
        this.sistemaDao = sistemaDao;
        this.propiedades = propiedades;
    }

    /** Si MySQL no responde, versionMysql queda a null y estado = "SIN_BASE_DE_DATOS" (el controlador da 503). */
    public EstadoApi comprobar() {
        String version;
        try {
            version = sistemaDao.versionBaseDatos();
        } catch (DataAccessException e) {
            version = null;
        }
        Runtime jvm = Runtime.getRuntime();
        return new EstadoApi(
                version != null ? "OK" : "SIN_BASE_DE_DATOS",
                propiedades.alumno(),
                propiedades.prestamo().maximo(),
                propiedades.prestamo().dias(),
                version,
                (jvm.totalMemory() - jvm.freeMemory()) / MIB,
                jvm.maxMemory() / MIB);
    }
}
