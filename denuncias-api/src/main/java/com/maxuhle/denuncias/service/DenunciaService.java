package com.maxuhle.denuncias.service;

import com.maxuhle.denuncias.dto.ConteoPorTipo;
import com.maxuhle.denuncias.dto.DenunciaRequest;
import com.maxuhle.denuncias.dto.DenunciaUpdateRequest;
import com.maxuhle.denuncias.dto.SeguimientoRequest;
import com.maxuhle.denuncias.exception.RecursoNoEncontradoException;
import com.maxuhle.denuncias.exception.ReglaNegocioException;
import com.maxuhle.denuncias.model.Denuncia;
import com.maxuhle.denuncias.model.Seguimiento;
import com.maxuhle.denuncias.model.Usuario;
import com.maxuhle.denuncias.repository.DenunciaRepository;
import com.maxuhle.denuncias.repository.SeguimientoRepository;
import com.maxuhle.denuncias.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public DenunciaService(DenunciaRepository denunciaRepository,
                           SeguimientoRepository seguimientoRepository,
                           UsuarioRepository usuarioRepository) {
        this.denunciaRepository = denunciaRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * TRANSACCIÓN 1: registra la denuncia y su primer seguimiento.
     * Si falla cualquiera de los dos inserts, no se guarda ninguno.
     */
    @Transactional
    public Denuncia crear(DenunciaRequest req, String username) {
        Usuario responsable = obtenerUsuario(username);

        Denuncia d = new Denuncia();
        d.setTipoDenuncia(req.tipoDenuncia().trim().toUpperCase());
        d.setDescripcion(req.descripcion());
        d.setNombreDenunciante(req.nombreDenunciante());
        d.setNombreAfectado(req.nombreAfectado());

        // @PrePersist define fecha, protocoloActivado y estado inicial
        Denuncia guardada = denunciaRepository.save(d);

        guardarSeguimiento(guardada, null, guardada.getEstado(), "Denuncia registrada", responsable);
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Denuncia> listar() {
        return denunciaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Denuncia obtener(Long id) {
        return obtenerEntidad(id);
    }

    @Transactional
    public Denuncia actualizarDescripcion(Long id, DenunciaUpdateRequest req) {
        Denuncia d = obtenerEntidad(id);
        d.setDescripcion(req.descripcion());
        return denunciaRepository.save(d);
    }

    /** TRANSACCIÓN 2: elimina el historial y la denuncia como una sola unidad. */
    @Transactional
    public void eliminar(Long id) {
        obtenerEntidad(id);
        seguimientoRepository.deleteByDenuncia_Id(id);
        denunciaRepository.deleteById(id);
    }

    // ---------- Consultas JPQL ----------

    @Transactional(readOnly = true)
    public List<Denuncia> listarPorEstado(String estado) {
        return denunciaRepository.buscarPorEstado(estado.trim().toUpperCase());
    }

    @Transactional(readOnly = true)
    public List<Denuncia> listarProtocolosPendientes() {
        return denunciaRepository.buscarProtocolosPendientes();
    }

    @Transactional(readOnly = true)
    public List<ConteoPorTipo> estadisticasPorTipo() {
        return denunciaRepository.contarPorTipo();
    }

    // ---------- Seguimiento ----------

    /**
     * TRANSACCIÓN 3 (principal): cambia el estado de la denuncia y registra el
     * seguimiento (con el usuario responsable) en una sola operación atómica.
     * Si alguna regla falla, no se modifica nada (rollback).
     *
     * Reglas de negocio:
     *  - Una denuncia CERRADA no admite más cambios.
     *  - Un protocolo activado no puede volver a RECIBIDA.
     *  - Para CERRAR se exige una observación que justifique el cierre.
     */
    @Transactional
    public Seguimiento registrarSeguimiento(Long id, SeguimientoRequest req, String username) {
        Usuario responsable = obtenerUsuario(username);
        Denuncia d = obtenerEntidad(id);
        String anterior = d.getEstado();
        String nuevo = req.estadoNuevo();

        if ("CERRADA".equals(anterior)) {
            throw new ReglaNegocioException("La denuncia ya está cerrada y no admite cambios de estado");
        }
        if (d.isProtocoloActivado() && "RECIBIDA".equals(nuevo)) {
            throw new ReglaNegocioException("Un protocolo activado no puede volver al estado RECIBIDA");
        }
        if ("CERRADA".equals(nuevo) && (req.observacion() == null || req.observacion().isBlank())) {
            throw new ReglaNegocioException("Para cerrar una denuncia debe registrar una observación");
        }

        d.setEstado(nuevo);
        denunciaRepository.save(d);

        return guardarSeguimiento(d, anterior, nuevo, req.observacion(), responsable);
    }

    @Transactional(readOnly = true)
    public List<Seguimiento> listarSeguimientos(Long id) {
        obtenerEntidad(id);
        return seguimientoRepository.findByDenuncia_IdOrderByFechaAsc(id);
    }

    // ---------- Auxiliares ----------

    private Denuncia obtenerEntidad(Long id) {
        return denunciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la denuncia con id " + id));
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + username));
    }

    private Seguimiento guardarSeguimiento(Denuncia d, String anterior, String nuevo,
                                           String observacion, Usuario responsable) {
        Seguimiento s = new Seguimiento();
        s.setDenuncia(d);
        s.setUsuario(responsable);
        s.setEstadoAnterior(anterior);
        s.setEstadoNuevo(nuevo);
        s.setObservacion(observacion);
        return seguimientoRepository.save(s);
    }
}
