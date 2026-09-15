package pe.edu.pucp.skillbridge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;
import pe.edu.pucp.skillbridge.entity.Habilidad;
import java.util.List;

@Repository
public interface HabilidadRepository extends JpaRepository<Habilidad, Integer> {
    List<Habilidad> findByEstadoTrueOrderByNombreAsc();

    @Query("SELECT h FROM Habilidad h WHERE " +
           "(:q IS NULL OR :q = '' OR LOWER(h.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(h.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:categoria IS NULL OR :categoria = '' OR h.categoria = :categoria) AND " +
           "(:soloActivas = false OR h.estado = true)")
    List<Habilidad> filtrarHabilidades(
        @Param("q") String q,
        @Param("categoria") String categoria,
        @Param("soloActivas") boolean soloActivas
    );
}
