package com.gamezdev.sicp.catalogos.repository;

import com.gamezdev.sicp.catalogos.entity.UnidadMedida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadRepository extends JpaRepository<UnidadMedida, Integer> {

    Page<UnidadMedida> findByCodigoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String codigo, String descripcion, Pageable pageable);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Integer id);
}