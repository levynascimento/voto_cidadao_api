package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.IdentificarEleitorResponse;
import br.com.urnaeletronica.entity.Cidadao;
import br.com.urnaeletronica.entity.Eleitor;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.repository.EleitorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do {@link EleitorServiceImpl}.
 *
 * <p>Cobre a identificação do eleitor pelo título, retornando seus dados ou
 * lançando exceção quando não encontrado.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EleitorServiceImpl - identificação do eleitor")
class EleitorServiceImplTest {

    @Mock
    private EleitorRepository eleitorRepository;

    @InjectMocks
    private EleitorServiceImpl eleitorService;

    @Test
    @DisplayName("Deve identificar o eleitor existente e retornar seus dados")
    void deveIdentificarEleitorExistente() {
        Cidadao cidadao = Cidadao.builder().cpf("11111111111").nome("Ana Silva").build();
        Eleitor eleitor = new Eleitor();
        eleitor.setId(1L);
        eleitor.setTituloEleitor("10001");
        eleitor.setJaVotou(false);
        eleitor.setCidadao(cidadao);

        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);

        IdentificarEleitorResponse response = eleitorService.identificarEleitor("10001");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.tituloEleitor()).isEqualTo("10001");
        assertThat(response.nome()).isEqualTo("Ana Silva");
        assertThat(response.cpf()).isEqualTo("11111111111");
        assertThat(response.jaVotou()).isFalse();
    }

    @Test
    @DisplayName("Deve indicar quando o eleitor já votou")
    void deveIndicarQuandoEleitorJaVotou() {
        Cidadao cidadao = Cidadao.builder().cpf("22222222222").nome("Bruno Costa").build();
        Eleitor eleitor = new Eleitor();
        eleitor.setId(2L);
        eleitor.setTituloEleitor("10002");
        eleitor.setJaVotou(true);
        eleitor.setCidadao(cidadao);

        when(eleitorRepository.findByTituloEleitor("10002")).thenReturn(eleitor);

        IdentificarEleitorResponse response = eleitorService.identificarEleitor("10002");

        assertThat(response.jaVotou()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando o eleitor não existir")
    void deveLancarExcecaoQuandoEleitorNaoEncontrado() {
        when(eleitorRepository.findByTituloEleitor("99999")).thenReturn(null);

        assertThatThrownBy(() -> eleitorService.identificarEleitor("99999"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Eleitor nao encontrado");
    }
}
