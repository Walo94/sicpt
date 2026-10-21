package com.gamezdev.sicp.diseno.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gamezdev.sicp.diseno.entity.UnidadMedida;


public interface UnidadRepository extends JpaRepository<UnidadMedida, Integer> {

    Page<UnidadMedida> findByCodigoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String codigo, String descripcion, Pageable pageable);

    Page<UnidadMedida> findByDescripcionContainingIgnoreCase(String descripcion, Pageable pageable);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Integer id);
}