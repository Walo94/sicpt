package com.gamezdev.sicp.mp.repository;

import com.gamezdev.sicp.mp.entity.AlmacenMp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlmacenRepository extends JpaRepository<AlmacenMp, Integer> {

    @Query("""
            SELECT a FROM AlmacenMp a
            WHERE LOWER(a.codigo) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<AlmacenMp> buscar(@Param("q") String q, Pageable pageable);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Integer id);
}