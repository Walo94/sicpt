package com.gamezdev.sicp.diseno.repository;

import com.gamezdev.sicp.diseno.entity.Combinacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CombinacionRepository extends JpaRepository<Combinacion, Integer> {

    @EntityGraph(attributePaths = {"modelo", "color", "talla", "unidadVenta"})
    @Query("""
            SELECT c FROM Combinacion c
            WHERE LOWER(c.codigoCombinacion) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(c.modelo.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(c.modelo.codigoModelo) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Combinacion> buscar(@Param("q") String q, Pageable pageable);

    boolean existsByCodigoCombinacionIgnoreCase(String codigo);

    boolean existsByCodigoCombinacionIgnoreCaseAndIdNot(String codigo, Integer id);
}