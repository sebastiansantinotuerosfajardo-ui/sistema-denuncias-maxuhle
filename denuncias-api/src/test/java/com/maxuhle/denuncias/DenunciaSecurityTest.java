package com.maxuhle.denuncias;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas automáticas de seguridad y reglas de negocio (usan el perfil h2, sin necesidad de MySQL). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class DenunciaSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String CON_PROTOCOLO = """
            {"tipoDenuncia":"MALTRATO_PSICOLOGICO","descripcion":"Caso de prueba",
             "nombreDenunciante":"Tester","nombreAfectado":"Estudiante X"}""";

    private static final String SIN_PROTOCOLO = """
            {"tipoDenuncia":"CONVIVENCIA","descripcion":"Caso de prueba",
             "nombreDenunciante":"Tester","nombreAfectado":"Estudiante Y"}""";

    @Test
    void endpointPublicoNoRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/api/publico/info"))
                .andExpect(status().isOk());
    }

    @Test
    void sinCredencialesDevuelve401() throws Exception {
        mockMvc.perform(get("/api/denuncias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operadorPuedeCrearYSeActivaElProtocolo() throws Exception {
        mockMvc.perform(post("/api/denuncias")
                        .with(httpBasic("operador", "operador123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CON_PROTOCOLO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.protocoloActivado").value(true))
                .andExpect(jsonPath("$.estado").value("PROTOCOLO_ACTIVADO"));
    }

    @Test
    void tipoSinProtocoloQuedaRecibida() throws Exception {
        mockMvc.perform(post("/api/denuncias")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SIN_PROTOCOLO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.protocoloActivado").value(false))
                .andExpect(jsonPath("$.estado").value("RECIBIDA"));
    }

    @Test
    void operadorNoPuedeEliminarDevuelve403() throws Exception {
        mockMvc.perform(delete("/api/denuncias/1")
                        .with(httpBasic("operador", "operador123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void operadorNoPuedeVerEstadisticasDevuelve403() throws Exception {
        mockMvc.perform(get("/api/denuncias/estadisticas/por-tipo")
                        .with(httpBasic("operador", "operador123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminConsultaEstadisticasDevuelve200() throws Exception {
        mockMvc.perform(get("/api/denuncias/estadisticas/por-tipo")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void adminEliminarInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/denuncias/99999")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void datosInvalidosDevuelven400() throws Exception {
        mockMvc.perform(post("/api/denuncias")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoDenuncia\":\"\",\"descripcion\":\"\",\"nombreAfectado\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
