package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.entity.Urna;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.repository.UrnaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrnaRepository urnaRepository;

    @BeforeEach
    void setUp() {
        urnaRepository.deleteAll();
        urnaRepository.save(Urna.builder().id(1L).estado(UrnaEstado.AGUARDANDO_INICIO).build());
    }

    @Test
    void deveInicializarVotacaoComSenhaValida() throws Exception {
        String request = """
                {
                  "senha": "TSE2024"
                }
                """;

        mockMvc.perform(post("/api/v1/votacao/inicializar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EM_VOTACAO"))
                .andExpect(jsonPath("$.mensagem").value("Votação iniciada com sucesso"));
    }

    @Test
    void deveRetornarForbiddenQuandoSenhaForInvalida() throws Exception {
        String request = """
                {
                  "senha": "ERRADA"
                }
                """;

        mockMvc.perform(post("/api/v1/votacao/inicializar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Senha do TSE inválida"));
    }
}
