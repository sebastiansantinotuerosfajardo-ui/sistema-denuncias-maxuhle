package com.maxuhle.denuncias.repository;

import com.maxuhle.denuncias.dto.ConteoPorTipo;
import com.maxuhle.denuncias.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    // JPQL 1: denuncias filtradas por estado, las más recientes primero
    @Query("SELECT d FROM Denuncia d WHERE d.estado = :estado ORDER BY d.fechaRegistro DESC")
    List<Denuncia> buscarPorEstado(@Param("estado") String estado);

    // JPQL 2: denuncias con protocolo activado que aún no se cierran (las más antiguas primero)
    @Query("SELECT d FROM Denuncia d WHERE d.protocoloActivado = true AND d.estado <> 'CERRADA' "
            + "ORDER BY d.fechaRegistro ASC")
    List<Denuncia> buscarProtocolosPendientes();

    // JPQL 3: cantidad de denuncias por tipo (reporte para dirección)
    @Query("SELECT new com.maxuhle.denuncias.dto.ConteoPorTipo(d.tipoDenuncia, COUNT(d)) "
            + "FROM Denuncia d GROUP BY d.tipoDenuncia ORDER BY COUNT(d) DESC")
    List<ConteoPorTipo> contarPorTipo();
}
