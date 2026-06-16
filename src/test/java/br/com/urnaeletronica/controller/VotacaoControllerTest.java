package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.dto.FinalizarVotacaoResponse;
import br.com.urnaeletronica.dto.InicializarVotacaoResponse;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.exception.SenhaInvalidaException;
import br.com.urnaeletronica.service.VotacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do {@link VotacaoController} usando MockMvc com o {@link VotacaoService} mockado.
 *
 * <p>Valida o mapeamento dos endpoints de inicialização/encerramento e a tradução
 * das exceções de negócio em códigos HTTP (400, 403, 404, 409).</p>
 */
@WebMvcTest(VotacaoController.class)
@DisplayName("VotacaoController - endpoints de inicialização e encerramento")
class VotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VotacaoService votacaoService;

    private static final String JSON_SENHA_VALIDA = "{\"senha\":\"TSE2024\"}";

    // ----------------------- INICIALIZAR -----------------------

    @Test
    @DisplayName("POST /inicializar deve retornar 200 com a urna em votação")
    void deveInicializarVotacaoComSucesso() throws Exception {
        when(votacaoService.inicializarVotacao(anyString())).thenReturn(
                InicializarVotacaoResponse.builder()
                        .mensagem("Votação iniciada com sucesso")
                        .estado(UrnaEstado.EM_VOTACAO)
                        .inicioVotacao(LocalDateTime.now())
                        .build());

        mockMvc.perform(post("/api/v1/votacao/inicializar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EM_VOTACAO"))
                .andExpect(jsonPath("$.mensagem").value("Votação iniciada com sucesso"));
    }

    @Test
    @DisplayName("POST /inicializar deve retornar 400 quando a senha estiver vazia")
    void deveRetornar400QuandoSenhaVazia() throws Exception {
        mockMvc.perform(post("/api/v1/votacao/inicializar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /inicializar deve retornar 403 quando a senha for inválida")
    void deveRetornar403QuandoSenhaInvalida() throws Exception {
        when(votacaoService.inicializarVotacao(anyString()))
                .thenThrow(new SenhaInvalidaException("Senha do TSE inválida"));

        mockMvc.perform(post("/api/v1/votacao/inicializar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"ERRADA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Senha do TSE inválida"));
    }

    @Test
    @DisplayName("POST /inicializar deve retornar 409 quando o estado da urna for inválido")
    void deveRetornar409QuandoEstadoInvalido() throws Exception {
        when(votacaoService.inicializarVotacao(anyString()))
                .thenThrow(new EstadoUrnaInvalidoException("A votação já está em andamento"));

        mockMvc.perform(post("/api/v1/votacao/inicializar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /inicializar deve retornar 404 quando a urna não for encontrada")
    void deveRetornar404QuandoUrnaNaoEncontrada() throws Exception {
        when(votacaoService.inicializarVotacao(anyString()))
                .thenThrow(new RecursoNaoEncontradoException("Urna não encontrada"));

        mockMvc.perform(post("/api/v1/votacao/inicializar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ----------------------- ENCERRAR -----------------------

    @Test
    @DisplayName("POST /encerrar deve retornar 200 com a urna finalizada")
    void deveEncerrarVotacaoComSucesso() throws Exception {
        when(votacaoService.finalizarVotacao(anyString())).thenReturn(
                FinalizarVotacaoResponse.builder()
                        .mensagem("Votação finalizada com sucesso.")
                        .estado(UrnaEstado.FINALIZADA)
                        .fimVotacao(LocalDateTime.now())
                        .build());

        mockMvc.perform(post("/api/v1/votacao/encerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"))
                .andExpect(jsonPath("$.fimVotacao").exists());
    }

    @Test
    @DisplayName("POST /encerrar deve retornar 403 quando a senha for inválida")
    void deveRetornar403AoEncerrarComSenhaInvalida() throws Exception {
        when(votacaoService.finalizarVotacao(anyString()))
                .thenThrow(new SenhaInvalidaException("Senha do TSE inválida"));

        mockMvc.perform(post("/api/v1/votacao/encerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"ERRADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /encerrar deve retornar 409 quando a votação não estiver em andamento")
    void deveRetornar409AoEncerrarComEstadoInvalido() throws Exception {
        when(votacaoService.finalizarVotacao(anyString()))
                .thenThrow(new EstadoUrnaInvalidoException("A votação não foi iniciada."));

        mockMvc.perform(post("/api/v1/votacao/encerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SENHA_VALIDA))
                .andExpect(status().isConflict());
    }
}
