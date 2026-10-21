package com.gamezdev.sicp.diseno.repository;

import com.gamezdev.sicp.diseno.entity.Modelo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModeloRepository extends JpaRepository<Modelo, Integer> {

    @EntityGraph(attributePaths = {"marca", "linea", "horma"})
    @Query("""
            SELECT m FROM Modelo m
            WHERE LOWER(m.codigoModelo) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Modelo> buscar(@Param("q") String q, Pageable pageable);

    boolean existsByCodigoModeloIgnoreCase(String codigo);

    boolean existsByCodigoModeloIgnoreCaseAndIdNot(String codigo, Integer id);
}