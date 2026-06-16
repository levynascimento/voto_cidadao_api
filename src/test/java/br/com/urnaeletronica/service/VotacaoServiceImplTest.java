package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.FinalizarVotacaoResponse;
import br.com.urnaeletronica.dto.InicializarVotacaoResponse;
import br.com.urnaeletronica.entity.Urna;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.exception.SenhaInvalidaException;
import br.com.urnaeletronica.repository.UrnaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do {@link VotacaoServiceImpl}.
 *
 * <p>Cobre a inicialização e o encerramento da votação, validando a senha do TSE,
 * as transições de estado da urna e os cenários de erro.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VotacaoServiceImpl - inicialização e encerramento da votação")
class VotacaoServiceImplTest {

    private static final long ID_URNA = 1L;
    private static final String SENHA_VALIDA = "TSE2024";

    @Mock
    private UrnaRepository urnaRepository;

    @InjectMocks
    private VotacaoServiceImpl votacaoService;

    @BeforeEach
    void setUp() {
        // Injeta a senha oficial (normalmente vinda de application.properties via @Value)
        ReflectionTestUtils.setField(votacaoService, "senhaOficialTse", SENHA_VALIDA);
    }

    private Urna urnaComEstado(UrnaEstado estado) {
        return Urna.builder().id(ID_URNA).estado(estado).build();
    }

    // ----------------------- INICIALIZAR -----------------------

    @Test
    @DisplayName("Deve iniciar a votação com senha válida e urna aguardando início")
    void deveInicializarVotacaoComSenhaValida() {
        Urna urna = urnaComEstado(UrnaEstado.AGUARDANDO_INICIO);
        when(urnaRepository.findById(ID_URNA)).thenReturn(Optional.of(urna));

        InicializarVotacaoResponse response = votacaoService.inicializarVotacao(SENHA_VALIDA);

        assertThat(response.estado()).isEqualTo(UrnaEstado.EM_VOTACAO);
        assertThat(response.mensagem()).isEqualTo("Votação iniciada com sucesso");
        assertThat(response.inicioVotacao()).isNotNull();
        assertThat(urna.getEstado()).isEqualTo(UrnaEstado.EM_VOTACAO);
        verify(urnaRepository).save(urna);
    }

    @Test
    @DisplayName("Deve lançar SenhaInvalidaException quando a senha for inválida")
    void deveLancarExcecaoQuandoSenhaInvalidaAoInicializar() {
        when(urnaRepository.findById(ID_URNA))
                .thenReturn(Optional.of(urnaComEstado(UrnaEstado.AGUARDANDO_INICIO)));

        assertThatThrownBy(() -> votacaoService.inicializarVotacao("SENHA_ERRADA"))
                .isInstanceOf(SenhaInvalidaException.class)
                .hasMessageContaining("Senha do TSE inválida");

        verify(urnaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando a urna não existir")
    void deveLancarExcecaoQuandoUrnaNaoEncontradaAoInicializar() {
        when(urnaRepository.findById(ID_URNA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> votacaoService.inicializarVotacao(SENHA_VALIDA))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Urna não encontrada");
    }

    @Test
    @DisplayName("Deve lançar EstadoUrnaInvalidoException ao inicializar urna já em votação")
    void deveLancarExcecaoQuandoUrnaJaEmVotacao() {
        when(urnaRepository.findById(ID_URNA))
                .thenReturn(Optional.of(urnaComEstado(UrnaEstado.EM_VOTACAO)));

        assertThatThrownBy(() -> votacaoService.inicializarVotacao(SENHA_VALIDA))
                .isInstanceOf(EstadoUrnaInvalidoException.class)
                .hasMessageContaining("já está em andamento");
    }

    @Test
    @DisplayName("Deve lançar EstadoUrnaInvalidoException ao inicializar urna já finalizada")
    void deveLancarExcecaoQuandoUrnaFinalizadaAoInicializar() {
        when(urnaRepository.findById(ID_URNA))
                .thenReturn(Optional.of(urnaComEstado(UrnaEstado.FINALIZADA)));

        assertThatThrownBy(() -> votacaoService.inicializarVotacao(SENHA_VALIDA))
                .isInstanceOf(EstadoUrnaInvalidoException.class)
                .hasMessageContaining("já foi finalizada");
    }

    // ----------------------- FINALIZAR -----------------------

    @Test
    @DisplayName("Deve finalizar a votação com senha válida e registrar a data de fim")
    void deveFinalizarVotacaoComSenhaValida() {
        Urna urna = urnaComEstado(UrnaEstado.EM_VOTACAO);
        when(urnaRepository.findById(ID_URNA)).thenReturn(Optional.of(urna));

        FinalizarVotacaoResponse response = votacaoService.finalizarVotacao(SENHA_VALIDA);

        assertThat(response.estado()).isEqualTo(UrnaEstado.FINALIZADA);
        assertThat(response.mensagem()).isEqualTo("Votação finalizada com sucesso.");
        // Regra corrigida: o encerramento deve gravar fimVotacao (e não inicioVotacao)
        assertThat(response.fimVotacao()).isNotNull();
        assertThat(urna.getFimVotacao()).isNotNull();
        assertThat(urna.getInicioVotacao()).isNull();
        assertThat(urna.getEstado()).isEqualTo(UrnaEstado.FINALIZADA);
        verify(urnaRepository).save(urna);
    }

    @Test
    @DisplayName("Deve lançar SenhaInvalidaException ao finalizar com senha inválida")
    void deveLancarExcecaoQuandoSenhaInvalidaAoFinalizar() {
        when(urnaRepository.findById(ID_URNA))
                .thenReturn(Optional.of(urnaComEstado(UrnaEstado.EM_VOTACAO)));

        assertThatThrownBy(() -> votacaoService.finalizarVotacao("OUTRA"))
                .isInstanceOf(SenhaInvalidaException.class);

        verify(urnaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Deve lançar EstadoUrnaInvalidoException ao finalizar urna ainda não iniciada")
    void deveLancarExcecaoQuandoUrnaNaoIniciadaAoFinalizar() {
        when(urnaRepository.findById(ID_URNA))
                .thenReturn(Optional.of(urnaComEstado(UrnaEstado.AGUARDANDO_INICIO)));

        assertThatThrownBy(() -> votacaoService.finalizarVotacao(SENHA_VALIDA))
                .isInstanceOf(EstadoUrnaInvalidoException.class)
                .hasMessageContaining("não foi iniciada");
    }

    @Test
    @DisplayName("Deve lançar EstadoUrnaInvalidoException ao finalizar urna já finalizada")
    void deveLancarExcecaoQuandoUrnaJaFinalizada() {
        when(urnaRepository.findById(ID_URNA))
                .thenReturn(Optional.of(urnaComEstado(UrnaEstado.FINALIZADA)));

        assertThatThrownBy(() -> votacaoService.finalizarVotacao(SENHA_VALIDA))
                .isInstanceOf(EstadoUrnaInvalidoException.class)
                .hasMessageContaining("já foi finalizada");
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao finalizar quando a urna não existir")
    void deveLancarExcecaoQuandoUrnaNaoEncontradaAoFinalizar() {
        when(urnaRepository.findById(ID_URNA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> votacaoService.finalizarVotacao(SENHA_VALIDA))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ----------------------- GARANTIR URNA INICIALIZADA -----------------------

    @Test
    @DisplayName("Deve criar a urna padrão quando ainda não existir")
    void deveCriarUrnaQuandoNaoExistir() {
        when(urnaRepository.existsById(ID_URNA)).thenReturn(false);

        votacaoService.garantirUrnaInicializada();

        ArgumentCaptor<Urna> captor = ArgumentCaptor.forClass(Urna.class);
        verify(urnaRepository).save(captor.capture());
        Urna salva = captor.getValue();
        assertThat(salva.getId()).isEqualTo(ID_URNA);
        assertThat(salva.getEstado()).isEqualTo(UrnaEstado.AGUARDANDO_INICIO);
    }

    @Test
    @DisplayName("Não deve criar nova urna quando ela já existir")
    void naoDeveCriarUrnaQuandoJaExistir() {
        when(urnaRepository.existsById(ID_URNA)).thenReturn(true);

        votacaoService.garantirUrnaInicializada();

        verify(urnaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
