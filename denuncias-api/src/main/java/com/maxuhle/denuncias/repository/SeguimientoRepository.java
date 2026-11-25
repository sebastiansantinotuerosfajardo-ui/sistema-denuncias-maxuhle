package com.maxuhle.denuncias.repository;

import com.maxuhle.denuncias.model.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    List<Seguimiento> findByDenuncia_IdOrderByFechaAsc(Long denunciaId);

    void deleteByDenuncia_Id(Long denunciaId);
}
