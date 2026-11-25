package com.maxuhle.denuncias.controller;

import com.maxuhle.denuncias.dto.ConteoPorTipo;
import com.maxuhle.denuncias.dto.DenunciaRequest;
import com.maxuhle.denuncias.dto.DenunciaUpdateRequest;
import com.maxuhle.denuncias.dto.SeguimientoRequest;
import com.maxuhle.denuncias.model.Denuncia;
import com.maxuhle.denuncias.model.Seguimiento;
import com.maxuhle.denuncias.service.DenunciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/denuncias")
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    // ---------- CRUD ----------

    @PostMapping
    public ResponseEntity<Denuncia> crear(@Valid @RequestBody DenunciaRequest request,
                                          Authentication auth) {
        Denuncia creada = denunciaService.crear(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    public List<Denuncia> listar() {
        return denunciaService.listar();
    }

    @GetMapping("/{id}")
    public Denuncia obtenerPorId(@PathVariable Long id) {
        return denunciaService.obtener(id);
    }

    @PutMapping("/{id}")
    public Denuncia actualizar(@PathVariable Long id,
                               @Valid @RequestBody DenunciaUpdateRequest request) {
        return denunciaService.actualizarDescripcion(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        denunciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Consultas JPQL ----------

    @GetMapping("/estado/{estado}")
    public List<Denuncia> porEstado(@PathVariable String estado) {
        return denunciaService.listarPorEstado(estado);
    }

    @GetMapping("/protocolo-pendiente")
    public List<Denuncia> protocolosPendientes() {
        return denunciaService.listarProtocolosPendientes();
    }

    @GetMapping("/estadisticas/por-tipo")
    public List<ConteoPorTipo> estadisticasPorTipo() {
        return denunciaService.estadisticasPorTipo();
    }

    // ---------- Seguimiento (operación transaccional) ----------

    @PostMapping("/{id}/seguimientos")
    public ResponseEntity<Seguimiento> registrarSeguimiento(@PathVariable Long id,
                                                            @Valid @RequestBody SeguimientoRequest request,
                                                            Authentication auth) {
        Seguimiento s = denunciaService.registrarSeguimiento(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(s);
    }

    @GetMapping("/{id}/seguimientos")
    public List<Seguimiento> listarSeguimientos(@PathVariable Long id) {
        return denunciaService.listarSeguimientos(id);
    }
}
