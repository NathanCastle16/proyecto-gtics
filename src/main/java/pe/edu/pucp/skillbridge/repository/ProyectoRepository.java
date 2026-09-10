package pe.edu.pucp.skillbridge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.pucp.skillbridge.entity.Proyecto;
import java.util.List;

public interface ProyectoRepository extends JpaRepository<Proyecto, Integer> {
    List<Proyecto> findByProjectManagerIdUsuarioOrderByFechaCreacionDesc(Integer idUsuario);
    List<Proyecto> findByEstadoOrderByFechaInicioDesc(Proyecto.EstadoProyecto estado);
    List<Proyecto> findByNombreContainingIgnoreCase(String nombre);
}
