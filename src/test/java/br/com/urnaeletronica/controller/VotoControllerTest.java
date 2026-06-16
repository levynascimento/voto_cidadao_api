package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.dto.VotarResponse;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.FraudeNaVotacaoException;
import br.com.urnaeletronica.exception.JaVotouException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.service.VotoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do {@link VotoController} usando MockMvc com o {@link VotoService} mockado.
 *
 * <p>Valida o registro de votos (sucesso e abstenção), a validação do payload
 * (400) e a tradução das exceções de negócio (404 eleitor; 409 já votou / fraude /
 * estado inválido).</p>
 */
@WebMvcTest(VotoController.class)
@DisplayName("VotoController - registro de votos")
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VotoService votoService;

    private static final String VOTO_VALIDO = """
            {
              "tituloEleitor": "10001",
              "votosEleitor": [
                { "cargoID": 1, "numeroCandidato": 10, "tipoVoto": "NORMAL" }
              ]
            }
            """;

    @Test
    @DisplayName("POST /votar deve retornar 200 quando o voto for computado")
    void deveRegistrarVotoComSucesso() throws Exception {
        when(votoService.votar(any())).thenReturn(
                new VotarResponse("Votação computada com sucesso!", "Ana Silva", LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/voto/votar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VOTO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Votação computada com sucesso!"))
                .andExpect(jsonPath("$.nomeEleitor").value("Ana Silva"));
    }

    @Test
    @DisplayName("POST /votar deve aceitar abstenção (lista de votos vazia)")
    void deveAceitarAbstencao() throws Exception {
        when(votoService.votar(any())).thenReturn(
                new VotarResponse("Votação computada com sucesso!", "Carlos Pereira", LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/voto/votar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloEleitor\":\"10003\",\"votosEleitor\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /votar deve retornar 400 quando o título do eleitor estiver ausente")
    void deveRetornar400QuandoTituloAusente() throws Exception {
        mockMvc.perform(post("/api/v1/voto/votar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"votosEleitor\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /votar deve retornar 404 quando o eleitor não existir")
    void deveRetornar404QuandoEleitorNaoEncontrado() throws Exception {
        when(votoService.votar(any()))
                .thenThrow(new RecursoNaoEncontradoException("Eleitor não encontrado."));

        mockMvc.perform(post("/api/v1/voto/votar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VOTO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /votar deve retornar 409 quando o eleitor já votou")
    void deveRetornar409QuandoEleitorJaVotou() throws Exception {
        when(votoService.votar(any())).thenThrow(new JaVotouException());

        mockMvc.perform(post("/api/v1/voto/votar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VOTO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("O eleitor já realizou sua votação"));
    }

    @Test
    @DisplayName("POST /votar deve retornar 409 quando houver fraude na votação")
    void deveRetornar409QuandoFraude() throws Exception {
        when(votoService.votar(any()))
                .thenThrow(new FraudeNaVotacaoException("Voto duplicado detectado"));

        mockMvc.perform(post("/api/v1/voto/votar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VOTO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Voto duplicado detectado"));
    }

    @Test
    @DisplayName("POST /votar deve retornar 409 quando a urna não estiver em votação")
    void deveRetornar409QuandoEstadoInvalido() throws Exception {
        when(votoService.votar(any()))
                .thenThrow(new EstadoUrnaInvalidoException("A votação ainda não foi iniciada."));

        mockMvc.perform(post("/api/v1/voto/votar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VOTO_VALIDO))
                .andExpect(status().isConflict());
    }
}
