package com.gamezdev.sicp.security.repository;

import com.gamezdev.sicp.security.entity.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    boolean existsByCodigoIgnoreCase(String codigo);

    List<Rol> findByEstatusOrderByNombreAsc(Integer estatus);

    @Query("""
            select r from Rol r
            where lower(r.codigo) like lower(concat('%', :q, '%'))
               or lower(r.nombre) like lower(concat('%', :q, '%'))
               or lower(coalesce(r.descripcion, '')) like lower(concat('%', :q, '%'))
            """)
    Page<Rol> buscar(@Param("q") String q, Pageable pageable);
}