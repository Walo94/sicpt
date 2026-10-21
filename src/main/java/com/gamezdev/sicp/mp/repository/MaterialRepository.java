package com.gamezdev.sicp.mp.repository;

import com.gamezdev.sicp.mp.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialRepository extends JpaRepository<Material, Integer> {

    @Query("""
            SELECT m FROM Material m
            WHERE LOWER(m.codigo) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Material> buscar(@Param("q") String q, Pageable pageable);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Integer id);
}