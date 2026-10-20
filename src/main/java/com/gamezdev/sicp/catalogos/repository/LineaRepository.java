package com.gamezdev.sicp.catalogos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.gamezdev.sicp.catalogos.entity.Linea;

@Repository
public interface LineaRepository extends JpaRepository<Linea, Integer> {
    Page<Linea> findByDescripcionContainingIgnoreCase(String descripcion, Pageable pageable);

    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdNot(String descripcion, Integer id);
}
