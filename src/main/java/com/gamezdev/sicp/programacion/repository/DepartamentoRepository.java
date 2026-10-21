package com.gamezdev.sicp.programacion.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gamezdev.sicp.programacion.entity.Departamento;


public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {
    Page<Departamento> findByDescripcionContainingIgnoreCase(String descripcion, Pageable pageable);

    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdNot(String descripcion, Integer id);
}