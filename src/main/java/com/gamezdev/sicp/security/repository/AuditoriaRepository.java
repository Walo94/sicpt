package com.gamezdev.sicp.security.repository;

import com.gamezdev.sicp.security.entity.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    /**
     * q y accion llegan en minúsculas / vacíos (no null); desde y hasta siempre
     * traen valor.
     */
    @Query("""
            select a from Auditoria a
            where a.fecha >= :desde and a.fecha < :hasta
              and (:accion = '' or a.accion = :accion)
              and (:q = ''
                   or lower(coalesce(a.username, '')) like concat('%', :q, '%')
                   or lower(coalesce(a.entidad, ''))  like concat('%', :q, '%')
                   or lower(coalesce(a.modulo, ''))   like concat('%', :q, '%')
                   or lower(coalesce(a.detalle, ''))  like concat('%', :q, '%'))
            """)
    Page<Auditoria> buscar(@Param("q") String q, @Param("accion") String accion,
            @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
            Pageable pageable);

    @Query("select distinct a.accion from Auditoria a order by a.accion")
    List<String> accionesDistintas();

}
