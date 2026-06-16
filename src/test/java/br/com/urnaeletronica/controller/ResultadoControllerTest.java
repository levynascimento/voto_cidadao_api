package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.dto.ResultadoResponse;
import br.com.urnaeletronica.dto.VencedoresResponse;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.SenhaInvalidaException;
import br.com.urnaeletronica.service.ResultadoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do {@link ResultadoController} usando MockMvc com o {@link ResultadoService} mockado.
 *
 * <p>Valida os endpoints de apuração e de vencedores, a obrigatoriedade da senha
 * (400), a senha inválida (403) e a exigência de urna finalizada (409).</p>
 */
@WebMvcTest(ResultadoController.class)
@DisplayName("ResultadoController - apuração e vencedores")
class ResultadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResultadoService resultadoService;

    private static final String JSON_SENHA_VALIDA = "{\"senha\":\"TSE2024\"}";

    @Test
    @DisplayName("POST /resultados deve retornar 200 com os resultados apurados")
    void deveApurarResultadosComSucesso() throws Exception {
        when(resultadoService.apurarResultados(anyString())).thenReturn(
                ResultadoResponse.builder()
                        .estadoUrna(UrnaEstado.FINALIZADA)
                        .parcial(false)
                        .geradoEm(LocalDateTime.now())
                        .resultados(List.of())
                        .build());

        mockMvc.perform(post("/api/v1/resultados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoUrna").value("FINALIZADA"))
                .andExpect(jsonPath("$.parcial").value(false));
    }

    @Test
    @DisplayName("POST /resultados/vencedores deve retornar 200 com os vencedores")
    void deveDeterminarVencedoresComSucesso() throws Exception {
        when(resultadoService.determinarVencedores(anyString())).thenReturn(
                VencedoresResponse.builder()
                        .estadoUrna(UrnaEstado.FINALIZADA)
                        .geradoEm(LocalDateTime.now())
                        .vencedoresPorCargo(List.of())
                        .build());

        mockMvc.perform(post("/api/v1/resultados/vencedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoUrna").value("FINALIZADA"));
    }

    @Test
    @DisplayName("POST /resultados deve retornar 400 quando a senha estiver ausente")
    void deveRetornar400QuandoSenhaAusente() throws Exception {
        mockMvc.perform(post("/api/v1/resultados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /resultados deve retornar 403 quando a senha for inválida")
    void deveRetornar403QuandoSenhaInvalida() throws Exception {
        when(resultadoService.apurarResultados(anyString()))
                .thenThrow(new SenhaInvalidaException("Senha do TSE inválida"));

        mockMvc.perform(post("/api/v1/resultados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"ERRADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /resultados deve retornar 409 quando a votação não estiver finalizada")
    void deveRetornar409QuandoUrnaNaoFinalizada() throws Exception {
        when(resultadoService.apurarResultados(anyString()))
                .thenThrow(new EstadoUrnaInvalidoException(
                        "As estatísticas só ficam disponíveis após o encerramento da votação."));

        mockMvc.perform(post("/api/v1/resultados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /resultados/vencedores deve retornar 409 quando a votação não estiver finalizada")
    void deveRetornar409NosVencedoresQuandoUrnaNaoFinalizada() throws Exception {
        when(resultadoService.determinarVencedores(anyString()))
                .thenThrow(new EstadoUrnaInvalidoException(
                        "As estatísticas só ficam disponíveis após o encerramento da votação."));

        mockMvc.perform(post("/api/v1/resultados/vencedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isConflict());
    }
}
