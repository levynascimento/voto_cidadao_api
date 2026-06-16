package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.ResultadoCargoResponse;
import br.com.urnaeletronica.dto.ResultadoResponse;
import br.com.urnaeletronica.dto.VencedoresResponse;
import br.com.urnaeletronica.entity.Candidato;
import br.com.urnaeletronica.entity.Cargo;
import br.com.urnaeletronica.entity.Cidadao;
import br.com.urnaeletronica.entity.Urna;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.exception.SenhaInvalidaException;
import br.com.urnaeletronica.repository.CandidatoRepository;
import br.com.urnaeletronica.repository.CargoRepository;
import br.com.urnaeletronica.repository.UrnaRepository;
import br.com.urnaeletronica.security.SenhaTseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do {@link ResultadoServiceImpl}.
 *
 * <p>Cobre a apuração de resultados e a determinação de vencedores, validando a
 * senha do TSE, a exigência de urna finalizada e o cálculo de percentuais,
 * votos brancos/nulos e candidatos eleitos.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResultadoServiceImpl - apuração e vencedores")
class ResultadoServiceImplTest {

    private static final long ID_URNA = 1L;
    private static final String SENHA_VALIDA = "TSE2024";
    private static final String SENHA_INVALIDA = "ERRADA";

    @Mock
    private CargoRepository cargoRepository;
    @Mock
    private CandidatoRepository candidatoRepository;
    @Mock
    private UrnaRepository urnaRepository;

    private ResultadoServiceImpl resultadoService;

    /**
     * Usamos uma instância REAL do {@link SenhaTseValidator} (e não um mock) porque
     * ele é uma classe concreta. Mockar classes concretas exige o "inline mock maker"
     * do Mockito, que manipula bytecode via agente Java e falha em alguns ambientes/JDKs
     * (notadamente ao rodar pelo IntelliJ), derrubando a inicialização de todos os mocks
     * da classe. Como o validador é simples e sem dependências, a instância real é mais
     * robusta e portável, comportando-se de forma idêntica em Maven e na IDE.
     */
    @BeforeEach
    void setUp() {
        SenhaTseValidator senhaTseValidator = new SenhaTseValidator(SENHA_VALIDA);
        resultadoService = new ResultadoServiceImpl(
                cargoRepository, candidatoRepository, urnaRepository, senhaTseValidator);
    }

    // ----------------------- Helpers -----------------------

    private void mockUrna(UrnaEstado estado) {
        when(urnaRepository.findById(ID_URNA))
                .thenReturn(Optional.of(Urna.builder().id(ID_URNA).estado(estado).build()));
    }

    private Candidato candidato(int numero, String nome, int votos) {
        Candidato c = new Candidato();
        c.setNumeroCandidato(numero);
        c.setNumeroVotos(votos);
        c.setCidadao(Cidadao.builder().cpf(String.valueOf(numero)).nome(nome).build());
        c.setFotoUrl("/images/" + numero + ".png");
        return c;
    }

    private Cargo cargoPresidente() {
        return Cargo.builder().id(1L).nome("Presidente").limiteVotos(1)
                .votosBrancos(1).votosNulos(0).build();
    }

    // ----------------------- VALIDAÇÃO DE SENHA -----------------------

    @Test
    @DisplayName("Deve lançar SenhaInvalidaException na apuração quando a senha for inválida")
    void deveLancarExcecaoQuandoSenhaInvalidaNaApuracao() {
        assertThatThrownBy(() -> resultadoService.apurarResultados(SENHA_INVALIDA))
                .isInstanceOf(SenhaInvalidaException.class)
                .hasMessageContaining("Senha do TSE inválida");

        // A senha é validada antes de qualquer acesso à urna
        verify(urnaRepository, never()).findById(ID_URNA);
    }

    @Test
    @DisplayName("Deve lançar SenhaInvalidaException nos vencedores quando a senha for inválida")
    void deveLancarExcecaoQuandoSenhaInvalidaNosVencedores() {
        assertThatThrownBy(() -> resultadoService.determinarVencedores(SENHA_INVALIDA))
                .isInstanceOf(SenhaInvalidaException.class)
                .hasMessageContaining("Senha do TSE inválida");
    }

    // ----------------------- ESTADO DA URNA -----------------------

    @Test
    @DisplayName("Não deve apurar resultados enquanto a votação não estiver finalizada")
    void naoDeveApurarQuandoUrnaNaoFinalizada() {
        mockUrna(UrnaEstado.EM_VOTACAO);

        assertThatThrownBy(() -> resultadoService.apurarResultados(SENHA_VALIDA))
                .isInstanceOf(EstadoUrnaInvalidoException.class)
                .hasMessageContaining("após o encerramento");

        verify(cargoRepository, never()).findAll();
    }

    @Test
    @DisplayName("Não deve determinar vencedores enquanto a votação não estiver finalizada")
    void naoDeveDeterminarVencedoresQuandoUrnaNaoFinalizada() {
        mockUrna(UrnaEstado.AGUARDANDO_INICIO);

        assertThatThrownBy(() -> resultadoService.determinarVencedores(SENHA_VALIDA))
                .isInstanceOf(EstadoUrnaInvalidoException.class);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando a urna não existir")
    void deveLancarExcecaoQuandoUrnaNaoEncontradaNaApuracao() {
        when(urnaRepository.findById(ID_URNA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultadoService.apurarResultados(SENHA_VALIDA))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Urna não encontrada");
    }

    // ----------------------- APURAÇÃO (US11/US12) -----------------------

    @Test
    @DisplayName("Deve apurar resultados com percentuais, brancos/nulos e candidato eleito")
    void deveApurarResultadosComUrnaFinalizada() {
        mockUrna(UrnaEstado.FINALIZADA);
        when(cargoRepository.findAll()).thenReturn(List.of(cargoPresidente()));
        when(candidatoRepository.findByCargoIdOrderByNumeroVotosDesc(1L)).thenReturn(List.of(
                candidato(10, "Caneta Azul", 3),
                candidato(20, "Carleto", 1),
                candidato(30, "Mbappé", 0)
        ));

        ResultadoResponse response = resultadoService.apurarResultados(SENHA_VALIDA);

        assertThat(response.estadoUrna()).isEqualTo(UrnaEstado.FINALIZADA);
        assertThat(response.parcial()).isFalse();
        assertThat(response.resultados()).hasSize(1);

        ResultadoCargoResponse cargo = response.resultados().get(0);
        assertThat(cargo.cargoNome()).isEqualTo("Presidente");
        assertThat(cargo.totalVotosValidos()).isEqualTo(4);
        assertThat(cargo.votosBrancos()).isEqualTo(1);
        assertThat(cargo.votosNulos()).isZero();
        assertThat(cargo.totalGeral()).isEqualTo(5);
        // 1 branco em 5 votos totais => 20%
        assertThat(cargo.percentualBrancos()).isEqualTo(20.0);

        // Candidato mais votado: 3/4 votos válidos = 75%, eleito
        assertThat(cargo.candidatos()).hasSize(3);
        assertThat(cargo.candidatos().get(0).numeroCandidato()).isEqualTo(10);
        assertThat(cargo.candidatos().get(0).percentualVotosValidos()).isEqualTo(75.0);
        assertThat(cargo.candidatos().get(0).eleito()).isTrue();
        assertThat(cargo.candidatos().get(1).eleito()).isFalse();

        // Apenas 1 vaga de presidente
        assertThat(cargo.vencedores()).hasSize(1);
        assertThat(cargo.vencedores().get(0).numeroCandidato()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve tratar cargo sem nenhum voto sem erros de divisão por zero")
    void deveApurarCargoSemVotos() {
        Cargo cargoVazio = Cargo.builder().id(1L).nome("Presidente").limiteVotos(1)
                .votosBrancos(0).votosNulos(0).build();
        mockUrna(UrnaEstado.FINALIZADA);
        when(cargoRepository.findAll()).thenReturn(List.of(cargoVazio));
        when(candidatoRepository.findByCargoIdOrderByNumeroVotosDesc(1L)).thenReturn(List.of(
                candidato(10, "Caneta Azul", 0)
        ));

        ResultadoResponse response = resultadoService.apurarResultados(SENHA_VALIDA);

        ResultadoCargoResponse cargo = response.resultados().get(0);
        assertThat(cargo.totalGeral()).isZero();
        assertThat(cargo.percentualBrancos()).isZero();
        assertThat(cargo.candidatos().get(0).percentualVotosValidos()).isZero();
        // Candidato com 0 votos não pode ser considerado eleito
        assertThat(cargo.candidatos().get(0).eleito()).isFalse();
        assertThat(cargo.vencedores()).isEmpty();
    }

    // ----------------------- VENCEDORES (US13) -----------------------

    @Test
    @DisplayName("Deve determinar os 2 vencedores de um cargo com 2 vagas")
    void deveDeterminarVencedoresParaCargoComDuasVagas() {
        Cargo deputado = Cargo.builder().id(2L).nome("Deputado Federal").limiteVotos(2)
                .votosBrancos(0).votosNulos(1).build();
        mockUrna(UrnaEstado.FINALIZADA);
        when(cargoRepository.findAll()).thenReturn(List.of(deputado));
        when(candidatoRepository.findByCargoIdOrderByNumeroVotosDesc(2L)).thenReturn(List.of(
                candidato(40, "Virgínia", 5),
                candidato(80, "Vini JR", 3),
                candidato(120, "Neymar Jr", 1)
        ));

        VencedoresResponse response = resultadoService.determinarVencedores(SENHA_VALIDA);

        assertThat(response.estadoUrna()).isEqualTo(UrnaEstado.FINALIZADA);
        assertThat(response.vencedoresPorCargo()).hasSize(1);
        assertThat(response.vencedoresPorCargo().get(0).vagas()).isEqualTo(2);
        assertThat(response.vencedoresPorCargo().get(0).vencedores()).hasSize(2);
        assertThat(response.vencedoresPorCargo().get(0).vencedores())
                .extracting("numeroCandidato")
                .containsExactly(40, 80);
    }
}
