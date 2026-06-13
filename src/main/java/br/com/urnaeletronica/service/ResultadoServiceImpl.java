package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.CandidatoResultadoResponse;
import br.com.urnaeletronica.dto.ResultadoCargoResponse;
import br.com.urnaeletronica.dto.ResultadoResponse;
import br.com.urnaeletronica.dto.VencedorCargoResponse;
import br.com.urnaeletronica.dto.VencedoresResponse;
import br.com.urnaeletronica.entity.Candidato;
import br.com.urnaeletronica.entity.Cargo;
import br.com.urnaeletronica.entity.Urna;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.repository.CandidatoRepository;
import br.com.urnaeletronica.repository.CargoRepository;
import br.com.urnaeletronica.repository.UrnaRepository;
import br.com.urnaeletronica.security.SenhaTseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementação da apuração de resultados (US11, US12, US13) com validação de
 * senha do TSE (US14).
 */
@Service
@RequiredArgsConstructor
public class ResultadoServiceImpl implements ResultadoService {

    private static final long ID_UNICO_URNA = 1L;

    private final CargoRepository cargoRepository;
    private final CandidatoRepository candidatoRepository;
    private final UrnaRepository urnaRepository;
    private final SenhaTseValidator senhaTseValidator;

    @Override
    @Transactional(readOnly = true)
    public ResultadoResponse apurarResultados(String senhaInformada) {
        // US14 - valida a senha antes de qualquer ação
        senhaTseValidator.validar(senhaInformada);

        // As estatísticas só são apresentadas após o término da votação.
        UrnaEstado estado = obterEstadoUrnaFinalizada();

        List<ResultadoCargoResponse> resultados = cargoRepository.findAll().stream()
                .sorted(Comparator.comparing(Cargo::getId))
                .map(this::apurarCargo)
                .toList();

        return ResultadoResponse.builder()
                .estadoUrna(estado)
                .parcial(false)
                .geradoEm(LocalDateTime.now())
                .resultados(resultados)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VencedoresResponse determinarVencedores(String senhaInformada) {
        // US14 - valida a senha antes de qualquer ação
        senhaTseValidator.validar(senhaInformada);

        // Os vencedores só são determinados após o término da votação.
        UrnaEstado estado = obterEstadoUrnaFinalizada();

        List<VencedorCargoResponse> vencedoresPorCargo = cargoRepository.findAll().stream()
                .sorted(Comparator.comparing(Cargo::getId))
                .map(cargo -> {
                    ResultadoCargoResponse resultado = apurarCargo(cargo);
                    return VencedorCargoResponse.builder()
                            .cargoId(cargo.getId())
                            .cargoNome(cargo.getNome())
                            .vagas(vagasDoCargo(cargo))
                            .vencedores(resultado.vencedores())
                            .build();
                })
                .toList();

        return VencedoresResponse.builder()
                .estadoUrna(estado)
                .geradoEm(LocalDateTime.now())
                .vencedoresPorCargo(vencedoresPorCargo)
                .build();
    }

    /**
     * Apura um cargo: ordena candidatos por votos (decrescente), calcula
     * percentuais sobre os votos válidos (US11), agrega brancos/nulos (US12) e
     * marca os eleitos (US13).
     */
    private ResultadoCargoResponse apurarCargo(Cargo cargo) {
        List<Candidato> candidatos = candidatoRepository.findByCargoIdOrderByNumeroVotosDesc(cargo.getId());

        int votosBrancos = valorOuZero(cargo.getVotosBrancos());
        int votosNulos = valorOuZero(cargo.getVotosNulos());

        int totalVotosValidos = candidatos.stream()
                .mapToInt(c -> valorOuZero(c.getNumeroVotos()))
                .sum();

        int totalGeral = totalVotosValidos + votosBrancos + votosNulos;
        int vagas = vagasDoCargo(cargo);

        // US13 - vencedores: os "vagas" candidatos mais votados (presidente=1, deputado=2).
        // Empate na última vaga é resolvido pela ordenação determinística do banco.
        List<CandidatoResultadoResponse> candidatosResultado = new ArrayList<>();
        List<CandidatoResultadoResponse> vencedores = new ArrayList<>();

        for (int i = 0; i < candidatos.size(); i++) {
            Candidato candidato = candidatos.get(i);
            int votos = valorOuZero(candidato.getNumeroVotos());
            boolean eleito = i < vagas && votos > 0;

            CandidatoResultadoResponse dto = CandidatoResultadoResponse.builder()
                    .numeroCandidato(candidato.getNumeroCandidato())
                    .nome(candidato.getCidadao() != null ? candidato.getCidadao().getNome() : null)
                    .fotoUrl(candidato.getFotoUrl())
                    .votos(votos)
                    .percentualVotosValidos(percentual(votos, totalVotosValidos))
                    .eleito(eleito)
                    .build();

            candidatosResultado.add(dto);
            if (eleito) {
                vencedores.add(dto);
            }
        }

        return ResultadoCargoResponse.builder()
                .cargoId(cargo.getId())
                .cargoNome(cargo.getNome())
                .totalVotosValidos(totalVotosValidos)
                .votosBrancos(votosBrancos)
                .votosNulos(votosNulos)
                .totalGeral(totalGeral)
                .percentualBrancos(percentual(votosBrancos, totalGeral))
                .percentualNulos(percentual(votosNulos, totalGeral))
                .candidatos(candidatosResultado)
                .vencedores(vencedores)
                .build();
    }

    private UrnaEstado obterEstadoUrnaFinalizada() {
        Urna urna = urnaRepository.findById(ID_UNICO_URNA)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Urna não encontrada"));
        if (urna.getEstado() != UrnaEstado.FINALIZADA) {
            throw new EstadoUrnaInvalidoException(
                    "As estatísticas só ficam disponíveis após o encerramento da votação.");
        }
        return urna.getEstado();
    }

    private int vagasDoCargo(Cargo cargo) {
        Integer limite = cargo.getLimiteVotos();
        return (limite == null || limite < 1) ? 1 : limite;
    }

    private int valorOuZero(Integer valor) {
        return valor == null ? 0 : valor;
    }

    /**
     * US11 - percentual com base no total informado (votos válidos para candidatos).
     * Retorna 0 quando a base é zero, arredondando para 2 casas decimais.
     */
    private double percentual(int parte, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(parte * 100.0 / total)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
