package com.gamezdev.sicp.security.repository;

import com.gamezdev.sicp.security.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

}
