package com.gamezdev.sicp.catalogos.repository;

import com.gamezdev.sicp.catalogos.entity.Marca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    Page<Marca> findByDescripcionContainingIgnoreCase(String descripcion, Pageable pageable);

    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdNot(String descripcion, Integer id);
}
