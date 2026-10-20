package com.gamezdev.sicp.diseno.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gamezdev.sicp.diseno.entity.Horma;

@Repository
public interface HormaRepository extends JpaRepository<Horma, Integer>{
    @Query("""
            SELECT h FROM Horma h
            WHERE LOWER(h.claveHorma) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(h.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Horma> buscar(@Param("q") String q, Pageable pageable);

    boolean existsByClaveHormaIgnoreCase(String clave);

    boolean existsByClaveHormaIgnoreCaseAndIdNot(String clave, Integer id);
}
