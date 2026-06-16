package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.VotarRequest;
import br.com.urnaeletronica.dto.VotarResponse;
import br.com.urnaeletronica.dto.VotoRequest;
import br.com.urnaeletronica.entity.Cargo;
import br.com.urnaeletronica.entity.Cidadao;
import br.com.urnaeletronica.entity.Eleitor;
import br.com.urnaeletronica.entity.Urna;
import br.com.urnaeletronica.enums.TipoVoto;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.FraudeNaVotacaoException;
import br.com.urnaeletronica.exception.JaVotouException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.repository.CandidatoRepository;
import br.com.urnaeletronica.repository.CargoRepository;
import br.com.urnaeletronica.repository.EleitorRepository;
import br.com.urnaeletronica.repository.UrnaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do {@link VotoServiceImpl}.
 *
 * <p>Cobre o registro de votos (normal, branco, nulo e abstenção), a validação
 * do estado da urna, a unicidade do voto por eleitor e as proteções antifraude
 * (candidato inexistente, voto sem candidato, cargo inexistente, voto duplicado
 * e limite de votos por cargo).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VotoServiceImpl - registro de votos e antifraude")
class VotoServiceImplTest {

    private static final long ID_URNA = 1L;
    private static final long CARGO_PRESIDENTE = 1L;
    private static final long CARGO_DEPUTADO = 2L;

    @Mock
    private EleitorRepository eleitorRepository;
    @Mock
    private CandidatoRepository candidatoRepository;
    @Mock
    private CargoRepository cargoRepository;
    @Mock
    private UrnaRepository urnaRepository;

    @InjectMocks
    private VotoServiceImpl votoService;

    // ----------------------- Helpers -----------------------

    private void mockUrna(UrnaEstado estado) {
        when(urnaRepository.findById(ID_URNA))
                .thenReturn(Optional.of(Urna.builder().id(ID_URNA).estado(estado).build()));
    }

    private Eleitor eleitorValido() {
        Cidadao cidadao = Cidadao.builder().cpf("11111111111").nome("Ana Silva").build();
        Eleitor eleitor = new Eleitor();
        eleitor.setId(1L);
        eleitor.setTituloEleitor("10001");
        eleitor.setJaVotou(false);
        eleitor.setCidadao(cidadao);
        return eleitor;
    }

    private VotoRequest voto(Long cargo, Long numero, TipoVoto tipo) {
        VotoRequest v = new VotoRequest();
        v.setCargoID(cargo);
        v.setNumeroCandidato(numero);
        v.setTipoVoto(tipo);
        return v;
    }

    private VotarRequest votarRequest(String titulo, VotoRequest... votos) {
        VotarRequest req = new VotarRequest();
        req.setTituloEleitor(titulo);
        req.setVotosEleitor(votos == null ? null : new ArrayList<>(Arrays.asList(votos)));
        return req;
    }

    private Cargo cargo(Long id, String nome, int limite) {
        return Cargo.builder().id(id).nome(nome).limiteVotos(limite).build();
    }

    // ----------------------- SUCESSO -----------------------

    @Test
    @DisplayName("Deve registrar voto NORMAL válido e marcar o eleitor como já votou")
    void deveRegistrarVotoNormalValido() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);
        when(candidatoRepository.existsByNumeroCandidatoAndCargoId(10L, CARGO_PRESIDENTE)).thenReturn(true);
        when(cargoRepository.findAllById(any()))
                .thenReturn(List.of(cargo(CARGO_PRESIDENTE, "Presidente", 1)));

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, 10L, TipoVoto.NORMAL));

        VotarResponse response = votoService.votar(req);

        assertThat(response.mensagem()).isEqualTo("Votação computada com sucesso!");
        assertThat(response.nomeEleitor()).isEqualTo("Ana Silva");
        assertThat(response.dataVotacao()).isNotNull();
        assertThat(eleitor.isJaVotou()).isTrue();
        verify(candidatoRepository).incrementarVoto(10L, CARGO_PRESIDENTE);
        verify(eleitorRepository).save(eleitor);
    }

    @Test
    @DisplayName("Deve registrar voto em BRANCO incrementando o contador do cargo")
    void deveRegistrarVotoBranco() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);
        when(cargoRepository.existsById(CARGO_PRESIDENTE)).thenReturn(true);
        when(cargoRepository.findAllById(any()))
                .thenReturn(List.of(cargo(CARGO_PRESIDENTE, "Presidente", 1)));

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, null, TipoVoto.BRANCO));

        votoService.votar(req);

        verify(cargoRepository).incrementarVotosBrancos(CARGO_PRESIDENTE);
        verify(candidatoRepository, never()).incrementarVoto(anyLong(), anyLong());
        assertThat(eleitor.isJaVotou()).isTrue();
    }

    @Test
    @DisplayName("Deve registrar voto NULO incrementando o contador do cargo")
    void deveRegistrarVotoNulo() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);
        when(cargoRepository.existsById(CARGO_PRESIDENTE)).thenReturn(true);
        when(cargoRepository.findAllById(any()))
                .thenReturn(List.of(cargo(CARGO_PRESIDENTE, "Presidente", 1)));

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, null, TipoVoto.NULO));

        votoService.votar(req);

        verify(cargoRepository).incrementarVotosNulos(CARGO_PRESIDENTE);
        assertThat(eleitor.isJaVotou()).isTrue();
    }

    @Test
    @DisplayName("Deve registrar voto válido para múltiplos cargos (presidente + 2 deputados)")
    void deveRegistrarVotosMultiplosCargos() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);
        when(candidatoRepository.existsByNumeroCandidatoAndCargoId(anyLong(), anyLong())).thenReturn(true);
        when(cargoRepository.findAllById(any())).thenReturn(List.of(
                cargo(CARGO_PRESIDENTE, "Presidente", 1),
                cargo(CARGO_DEPUTADO, "Deputado Federal", 2)
        ));

        VotarRequest req = votarRequest("10001",
                voto(CARGO_PRESIDENTE, 10L, TipoVoto.NORMAL),
                voto(CARGO_DEPUTADO, 40L, TipoVoto.NORMAL),
                voto(CARGO_DEPUTADO, 80L, TipoVoto.NORMAL));

        votoService.votar(req);

        verify(candidatoRepository).incrementarVoto(10L, CARGO_PRESIDENTE);
        verify(candidatoRepository).incrementarVoto(40L, CARGO_DEPUTADO);
        verify(candidatoRepository).incrementarVoto(80L, CARGO_DEPUTADO);
        assertThat(eleitor.isJaVotou()).isTrue();
    }

    @Test
    @DisplayName("Deve permitir abstenção com lista de votos vazia")
    void devePermitirAbstencaoComListaVazia() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);

        VotarRequest req = votarRequest("10001");
        req.setVotosEleitor(Collections.emptyList());

        votoService.votar(req);

        assertThat(eleitor.isJaVotou()).isTrue();
        verify(candidatoRepository, never()).incrementarVoto(anyLong(), anyLong());
        verify(cargoRepository, never()).incrementarVotosBrancos(anyLong());
        verify(cargoRepository, never()).incrementarVotosNulos(anyLong());
        verify(eleitorRepository).save(eleitor);
    }

    @Test
    @DisplayName("Deve permitir abstenção quando a lista de votos for nula")
    void devePermitirAbstencaoComListaNula() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);

        VotarRequest req = votarRequest("10001");
        req.setVotosEleitor(null);

        votoService.votar(req);

        assertThat(eleitor.isJaVotou()).isTrue();
        verify(eleitorRepository).save(eleitor);
    }

    // ----------------------- ESTADO DA URNA -----------------------

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando a urna não existir")
    void deveLancarExcecaoQuandoUrnaNaoEncontrada() {
        when(urnaRepository.findById(ID_URNA)).thenReturn(Optional.empty());

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, 10L, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Urna não encontrada");
    }

    @Test
    @DisplayName("Deve impedir voto quando a votação ainda não foi iniciada")
    void deveImpedirVotoQuandoUrnaAguardandoInicio() {
        mockUrna(UrnaEstado.AGUARDANDO_INICIO);

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, 10L, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(EstadoUrnaInvalidoException.class)
                .hasMessageContaining("ainda não foi iniciada");
        verify(eleitorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve impedir voto quando a votação já foi finalizada")
    void deveImpedirVotoQuandoUrnaFinalizada() {
        mockUrna(UrnaEstado.FINALIZADA);

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, 10L, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(EstadoUrnaInvalidoException.class)
                .hasMessageContaining("já foi finalizada");
    }

    // ----------------------- VALIDAÇÃO DO ELEITOR -----------------------

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando o eleitor não existir")
    void deveLancarExcecaoQuandoEleitorNaoEncontrado() {
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("99999")).thenReturn(null);

        VotarRequest req = votarRequest("99999", voto(CARGO_PRESIDENTE, 10L, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Eleitor não encontrado");
    }

    @Test
    @DisplayName("Deve lançar JaVotouException quando o eleitor já votou")
    void deveLancarExcecaoQuandoEleitorJaVotou() {
        Eleitor eleitor = eleitorValido();
        eleitor.setJaVotou(true);
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, 10L, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(JaVotouException.class);
        verify(eleitorRepository, never()).save(any());
    }

    // ----------------------- ANTIFRAUDE -----------------------

    @Test
    @DisplayName("Deve lançar FraudeNaVotacaoException quando o candidato não concorre ao cargo")
    void deveLancarExcecaoQuandoCandidatoInexistente() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);
        when(candidatoRepository.existsByNumeroCandidatoAndCargoId(99L, CARGO_PRESIDENTE)).thenReturn(false);

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, 99L, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(FraudeNaVotacaoException.class)
                .hasMessageContaining("não está concorrendo ao cargo");
        verify(eleitorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar FraudeNaVotacaoException quando o voto normal não informar o candidato")
    void deveLancarExcecaoQuandoVotoNormalSemCandidato() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);

        VotarRequest req = votarRequest("10001", voto(CARGO_PRESIDENTE, null, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(FraudeNaVotacaoException.class)
                .hasMessageContaining("exige o número do candidato");
    }

    @Test
    @DisplayName("Deve lançar FraudeNaVotacaoException quando o cargo do voto branco/nulo não existir")
    void deveLancarExcecaoQuandoCargoInexistenteEmVotoBranco() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);
        when(cargoRepository.existsById(999L)).thenReturn(false);

        VotarRequest req = votarRequest("10001", voto(999L, null, TipoVoto.BRANCO));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(FraudeNaVotacaoException.class)
                .hasMessageContaining("não existe");
    }

    @Test
    @DisplayName("Deve lançar FraudeNaVotacaoException quando houver voto duplicado no mesmo candidato")
    void deveLancarExcecaoQuandoVotoDuplicado() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);
        when(candidatoRepository.existsByNumeroCandidatoAndCargoId(40L, CARGO_DEPUTADO)).thenReturn(true);

        VotarRequest req = votarRequest("10001",
                voto(CARGO_DEPUTADO, 40L, TipoVoto.NORMAL),
                voto(CARGO_DEPUTADO, 40L, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(FraudeNaVotacaoException.class)
                .hasMessageContaining("Voto duplicado");
        verify(eleitorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar FraudeNaVotacaoException quando o limite de votos do cargo for excedido")
    void deveLancarExcecaoQuandoLimitePorCargoExcedido() {
        Eleitor eleitor = eleitorValido();
        mockUrna(UrnaEstado.EM_VOTACAO);
        when(eleitorRepository.findByTituloEleitor("10001")).thenReturn(eleitor);
        when(candidatoRepository.existsByNumeroCandidatoAndCargoId(anyLong(), eq(CARGO_DEPUTADO))).thenReturn(true);
        when(cargoRepository.findAllById(any()))
                .thenReturn(List.of(cargo(CARGO_DEPUTADO, "Deputado Federal", 2)));

        VotarRequest req = votarRequest("10001",
                voto(CARGO_DEPUTADO, 40L, TipoVoto.NORMAL),
                voto(CARGO_DEPUTADO, 80L, TipoVoto.NORMAL),
                voto(CARGO_DEPUTADO, 120L, TipoVoto.NORMAL));

        assertThatThrownBy(() -> votoService.votar(req))
                .isInstanceOf(FraudeNaVotacaoException.class)
                .hasMessageContaining("permite no máximo");
        verify(eleitorRepository, never()).save(any());
    }
}
