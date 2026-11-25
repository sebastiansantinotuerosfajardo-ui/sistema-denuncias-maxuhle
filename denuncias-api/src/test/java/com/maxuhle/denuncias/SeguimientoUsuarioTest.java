package com.maxuhle.denuncias;

import com.jayway.jsonpath.JsonPath;
import com.maxuhle.denuncias.model.Seguimiento;
import com.maxuhle.denuncias.repository.SeguimientoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cubre el cambio del Avance 2: seguimiento.usuario_id (FK a usuario),
 * la transaccion de cambio de estado y su rollback. Usa el perfil h2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SeguimientoUsuarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    /** Crea una denuncia con el usuario indicado y devuelve su id. */
    private long crearDenuncia(String usuario, String clave, String tipo) throws Exception {
        String body = "{\"tipoDenuncia\":\"" + tipo + "\",\"descripcion\":\"Prueba FK\","
                + "\"nombreDenunciante\":\"Tester\",\"nombreAfectado\":\"Estudiante Z\"}";
        String json = mockMvc.perform(post("/api/denuncias")
                        .with(httpBasic(usuario, clave))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    @Test
    void primerSeguimientoQuedaEnlazadoAlUsuarioQueRegistra() throws Exception {
        long id = crearDenuncia("operador", "operador123", "CONVIVENCIA");

        mockMvc.perform(get("/api/denuncias/" + id + "/seguimientos")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].responsable").value("operador"))
                .andExpect(jsonPath("$[0].estadoNuevo").value("RECIBIDA"))
                .andExpect(jsonPath("$[0].denunciaId").value((int) id))
                // la contrasena cifrada y el objeto usuario no deben salir en el JSON
                .andExpect(jsonPath("$[0].usuario").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void cambioDeEstadoRegistraAlAdminComoResponsable() throws Exception {
        long id = crearDenuncia("admin", "admin123", "MALTRATO_PSICOLOGICO");

        mockMvc.perform(post("/api/denuncias/" + id + "/seguimientos")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoNuevo\":\"EN_ATENCION\",\"observacion\":\"Se cito a los involucrados\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsable").value("admin"))
                .andExpect(jsonPath("$.estadoAnterior").value("PROTOCOLO_ACTIVADO"))
                .andExpect(jsonPath("$.estadoNuevo").value("EN_ATENCION"))
                .andExpect(jsonPath("$.usuario").doesNotExist());

        mockMvc.perform(get("/api/denuncias/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_ATENCION"));
    }

    @Test
    void reglaRechazadaHaceRollbackYNoCambiaEstadoNiGuardaSeguimiento() throws Exception {
        long id = crearDenuncia("admin", "admin123", "CONVIVENCIA");

        // Cerrar sin observacion viola una regla de negocio -> 409
        mockMvc.perform(post("/api/denuncias/" + id + "/seguimientos")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoNuevo\":\"CERRADA\"}"))
                .andExpect(status().isConflict());

        // El estado sigue igual y solo existe el seguimiento inicial
        mockMvc.perform(get("/api/denuncias/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.estado").value("RECIBIDA"));
        mockMvc.perform(get("/api/denuncias/" + id + "/seguimientos").with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void laBaseGuardaLaClaveForaneaHaciaUsuario() throws Exception {
        long id = crearDenuncia("admin", "admin123", "CONVIVENCIA");

        List<Seguimiento> lista = seguimientoRepository.findByDenuncia_IdOrderByFechaAsc(id);

        assertEquals(1, lista.size());
        // usuario es EAGER, se puede leer fuera de la transaccion
        assertEquals("admin", lista.get(0).getUsuario().getUsername());
    }

    @Test
    void eliminarDenunciaBorraTambienSusSeguimientos() throws Exception {
        long id = crearDenuncia("admin", "admin123", "CONVIVENCIA");

        mockMvc.perform(delete("/api/denuncias/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        assertTrue(seguimientoRepository.findByDenuncia_IdOrderByFechaAsc(id).isEmpty());
    }
}
