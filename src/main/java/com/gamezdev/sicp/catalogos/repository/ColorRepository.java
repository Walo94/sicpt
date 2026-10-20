package com.gamezdev.sicp.catalogos.repository;

import com.gamezdev.sicp.catalogos.entity.Color;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {
    Page<Color> findByDescripcionContainingIgnoreCase(String descripcion, Pageable pageable);

    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdNot(String descripcion, Integer id);
}