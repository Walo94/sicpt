package com.gamezdev.sicp.diseno.repository;

import com.gamezdev.sicp.diseno.entity.ModeloBom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface BomRepository extends JpaRepository<ModeloBom, Integer> {

    // Solo trae la versión más alta (última) de cada modelo
    @EntityGraph(attributePaths = {"modelo"})
    @Query("""
            SELECT b FROM ModeloBom b
            WHERE b.version = (SELECT MAX(b2.version) FROM ModeloBom b2 WHERE b2.modelo.id = b.modelo.id)
              AND (LOWER(b.modelo.codigoModelo) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(b.modelo.descripcion) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<ModeloBom> buscarUltimasVersiones(@Param("q") String q, Pageable pageable);

    // Trae todas las versiones de un modelo para el reporte
    @EntityGraph(attributePaths = {"modelo", "consumos", "consumos.material"})
    java.util.List<ModeloBom> findByModeloIdOrderByVersionDesc(Integer modeloId);

    // Para saber la última versión al crear
    Optional<ModeloBom> findFirstByModeloIdOrderByVersionDesc(Integer modeloId);
}