package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.VotarRequest;
import br.com.urnaeletronica.dto.VotarResponse;
import br.com.urnaeletronica.dto.VotoRequest;
import br.com.urnaeletronica.enums.TipoVoto;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.entity.Cargo;
import br.com.urnaeletronica.entity.Eleitor;
import br.com.urnaeletronica.entity.Urna;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.FraudeNaVotacaoException;
import br.com.urnaeletronica.exception.JaVotouException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.repository.CandidatoRepository;
import br.com.urnaeletronica.repository.CargoRepository;
import br.com.urnaeletronica.repository.EleitorRepository;
import br.com.urnaeletronica.repository.UrnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VotoServiceImpl implements VotoService {

    private static final long ID_UNICO_URNA = 1L;

    private final EleitorRepository eleitorRepository;
    private final CandidatoRepository candidatoRepository;
    private final CargoRepository cargoRepository;
    private final UrnaRepository urnaRepository;

    @Override
    @Transactional
    public VotarResponse votar(VotarRequest request) {
        // A votação só pode ocorrer enquanto a urna estiver EM_VOTACAO (US01/US10).
        validarUrnaEmVotacao();

        Eleitor eleitor = eleitorRepository.findByTituloEleitor(request.getTituloEleitor());

        validarEleitor(eleitor);
        validarVotacao(request);

        // Voto em lista vazia representa uma abstenção: o eleitor é marcado como
        // "já votou" sem que nenhum voto seja computado.
        List<VotoRequest> votos = request.getVotosEleitor() == null
                ? Collections.emptyList()
                : request.getVotosEleitor();

        for (VotoRequest voto : votos) {
            switch (voto.getTipoVoto()) {
                case BRANCO:
                    cargoRepository.incrementarVotosBrancos(voto.getCargoID());
                    break;
                case NULO:
                    cargoRepository.incrementarVotosNulos(voto.getCargoID());
                    break;
                default:
                    candidatoRepository.incrementarVoto(voto.getNumeroCandidato(), voto.getCargoID());
                    break;
            }
        }
        eleitor.setJaVotou(true);
        eleitorRepository.save(eleitor);

        return new VotarResponse("Votação computada com sucesso!", eleitor.getCidadao().getNome(), LocalDateTime.now());
    }

    private void validarUrnaEmVotacao() {
        Urna urna = urnaRepository.findById(ID_UNICO_URNA)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Urna não encontrada."));

        if (urna.getEstado() == UrnaEstado.AGUARDANDO_INICIO) {
            throw new EstadoUrnaInvalidoException("A votação ainda não foi iniciada.");
        }
        if (urna.getEstado() == UrnaEstado.FINALIZADA) {
            throw new EstadoUrnaInvalidoException("A votação já foi finalizada.");
        }
    }

    private void validarEleitor(Eleitor eleitor) throws JaVotouException, RecursoNaoEncontradoException {
        if (eleitor == null) {
            throw new RecursoNaoEncontradoException("Eleitor não encontrado.");
        }
        if (eleitor.isJaVotou()) {
            throw new JaVotouException();
        }
    }

    /**
     * Garante que todo voto NORMAL informe o número do candidato e que esse candidato
     * realmente concorra ao cargo escolhido. Também valida que o cargo existe para
     * votos em branco/nulo.
     */
    private void validarCandidatosExistentes(VotarRequest request) throws FraudeNaVotacaoException {
        for (VotoRequest votoRequest : request.getVotosEleitor()) {
            if (votoRequest.getTipoVoto() != TipoVoto.NORMAL) {
                // Branco/Nulo não tem candidato, mas o cargo precisa existir.
                if (!cargoRepository.existsById(votoRequest.getCargoID())) {
                    throw new FraudeNaVotacaoException(
                            "Cargo informado (ID " + votoRequest.getCargoID() + ") não existe."
                    );
                }
                continue;
            }

            if (votoRequest.getNumeroCandidato() == null) {
                throw new FraudeNaVotacaoException(
                        "Voto normal exige o número do candidato para o cargo (ID "
                                + votoRequest.getCargoID() + ")."
                );
            }

            boolean candidatoExiste = candidatoRepository.existsByNumeroCandidatoAndCargoId(
                    votoRequest.getNumeroCandidato(), votoRequest.getCargoID()
            );

            if (!candidatoExiste) {
                throw new FraudeNaVotacaoException(
                        "Candidato de número " + votoRequest.getNumeroCandidato()
                                + " não está concorrendo ao cargo informado (ID " + votoRequest.getCargoID() + ")"
                );
            }
        }
    }

    /**
     * Impede que o mesmo candidato receba mais de um voto do mesmo eleitor para o mesmo cargo.
     */
    private void validarVotosDuplicados(VotarRequest request) throws FraudeNaVotacaoException {
        Map<Long, Set<Long>> candidatosVotadosPorCargo = new HashMap<>();

        for (VotoRequest votoRequest : request.getVotosEleitor()) {
            if (votoRequest.getTipoVoto() == TipoVoto.BRANCO || votoRequest.getTipoVoto() == TipoVoto.NULO) {
                continue;
            }

            Long cargoId = votoRequest.getCargoID();
            Long candidatoNumero = votoRequest.getNumeroCandidato();

            candidatosVotadosPorCargo.putIfAbsent(cargoId, new HashSet<>());
            Set<Long> candidatos = candidatosVotadosPorCargo.get(cargoId);

            if (!candidatos.add(candidatoNumero)) {
                throw new FraudeNaVotacaoException(
                        "Voto duplicado detectado: O candidato ao cargo " + cargoId + " de número "
                                + candidatoNumero + " recebeu mais de um voto para o mesmo cargo."
                );
            }
        }
    }

    /**
     * Garante que o total de votos depositados em cada cargo (somando votos normais,
     * brancos e nulos) não ultrapasse o limite de vagas daquele cargo
     * (ex.: 1 para Presidente, 2 para Deputado Federal).
     */
    private void validarLimitePorCargo(VotarRequest request) throws FraudeNaVotacaoException {
        Map<Long, Integer> votosPorCargo = new HashMap<>();
        for (VotoRequest votoRequest : request.getVotosEleitor()) {
            votosPorCargo.merge(votoRequest.getCargoID(), 1, Integer::sum);
        }

        List<Cargo> cargos = cargoRepository.findAllById(votosPorCargo.keySet());
        Map<Long, Cargo> cargosPorId = new HashMap<>();
        for (Cargo cargo : cargos) {
            cargosPorId.put(cargo.getId(), cargo);
        }

        for (Map.Entry<Long, Integer> entry : votosPorCargo.entrySet()) {
            Cargo cargo = cargosPorId.get(entry.getKey());
            if (cargo == null) {
                throw new FraudeNaVotacaoException("Cargo informado (ID " + entry.getKey() + ") não existe.");
            }
            int limite = cargo.getLimiteVotos() == null ? 1 : cargo.getLimiteVotos();
            if (entry.getValue() > limite) {
                throw new FraudeNaVotacaoException(
                        "O eleitor tentou depositar " + entry.getValue() + " votos no cargo '"
                                + cargo.getNome() + "', que permite no máximo " + limite + "."
                );
            }
        }
    }

    private void validarVotacao(VotarRequest request) throws FraudeNaVotacaoException {
        if (request.getVotosEleitor() == null || request.getVotosEleitor().isEmpty()) {
            // Abstenção: nada a validar.
            return;
        }
        validarCandidatosExistentes(request);
        validarVotosDuplicados(request);
        validarLimitePorCargo(request);
    }
}
