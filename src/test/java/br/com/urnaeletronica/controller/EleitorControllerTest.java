package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.dto.IdentificarEleitorResponse;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.service.EleitorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do {@link EleitorController} usando MockMvc com o {@link EleitorService} mockado.
 *
 * <p>Valida a identificação do eleitor (sucesso), a validação do payload (400) e
 * o tratamento de eleitor não encontrado (404).</p>
 */
@WebMvcTest(EleitorController.class)
@DisplayName("EleitorController - identificação de eleitor")
class EleitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EleitorService eleitorService;

    @Test
    @DisplayName("POST /identificar deve retornar 200 com os dados do eleitor")
    void deveIdentificarEleitorComSucesso() throws Exception {
        when(eleitorService.identificarEleitor(anyString())).thenReturn(
                IdentificarEleitorResponse.builder()
                        .id(1L)
                        .tituloEleitor("10001")
                        .nome("Ana Silva")
                        .cpf("11111111111")
                        .jaVotou(false)
                        .build());

        mockMvc.perform(post("/api/v1/eleitor/identificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloEleitor\":\"10001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.jaVotou").value(false));
    }

    @Test
    @DisplayName("POST /identificar deve retornar 400 quando o título estiver vazio")
    void deveRetornar400QuandoTituloVazio() throws Exception {
        mockMvc.perform(post("/api/v1/eleitor/identificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloEleitor\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /identificar deve retornar 404 quando o eleitor não existir")
    void deveRetornar404QuandoEleitorNaoEncontrado() throws Exception {
        when(eleitorService.identificarEleitor(anyString()))
                .thenThrow(new RecursoNaoEncontradoException("Eleitor nao encontrado"));

        mockMvc.perform(post("/api/v1/eleitor/identificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloEleitor\":\"99999\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Eleitor nao encontrado"));
    }
}
