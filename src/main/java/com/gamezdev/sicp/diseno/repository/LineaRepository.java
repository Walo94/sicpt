package com.gamezdev.sicp.diseno.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gamezdev.sicp.diseno.entity.Linea;


public interface LineaRepository extends JpaRepository<Linea, Integer> {
    Page<Linea> findByDescripcionContainingIgnoreCase(String descripcion, Pageable pageable);

    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdNot(String descripcion, Integer id);
}
