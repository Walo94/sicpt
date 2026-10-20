package com.gamezdev.sicp.security.repository;

import com.gamezdev.sicp.security.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    @Query("""
            select u from Usuario u
            where lower(u.username) like lower(concat('%', :q, '%'))
               or lower(u.nombre) like lower(concat('%', :q, '%'))
               or lower(coalesce(u.apellidos, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(u.email, '')) like lower(concat('%', :q, '%'))
            """)
    Page<Usuario> buscar(@Param("q") String q, Pageable pageable);

    /** Transacción propia y corta: se confirma (commit) al terminar el método. */
    @Transactional
    @Modifying
    @Query("update Usuario u set u.ultimoAcceso = :fecha where u.id = :id")
    void actualizarUltimoAcceso(@Param("id") Integer id, @Param("fecha") LocalDateTime fecha);
}
