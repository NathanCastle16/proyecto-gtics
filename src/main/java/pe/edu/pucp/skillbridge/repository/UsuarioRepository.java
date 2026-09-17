package pe.edu.pucp.skillbridge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.pucp.skillbridge.entity.Usuario;
import pe.edu.pucp.skillbridge.entity.Usuario.EstadoUsuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    List<Usuario> findByRolNombre(String nombre);
    Optional<Usuario> findFirstByRolNombreOrderByIdUsuarioAsc(String nombre);
    Optional<Usuario> findByCorreo(String correo);

    @Query("""
            SELECT u FROM Usuario u WHERE
            (
                :q IS NULL OR :q = '' OR
                LOWER(u.nombres) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(u.correo) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(u.rol.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
            ) AND (
                :rol IS NULL OR
                u.rol.idRol = :rol
            ) AND (
                :estado IS NULL OR
                :estado = u.estado
            )
        """)
    List<Usuario> filtrarUsuarios(
        @Param("q") String q,
        @Param("rol") Integer rol,
        @Param("estado") EstadoUsuario estado
    );
}
