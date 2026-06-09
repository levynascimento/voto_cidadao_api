package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.VotarRequest;
import br.com.urnaeletronica.dto.VotarResponse;
import br.com.urnaeletronica.dto.VotoRequest;
import br.com.urnaeletronica.enums.TipoVoto;
import br.com.urnaeletronica.entity.Cargo;
import br.com.urnaeletronica.entity.Eleitor;
import br.com.urnaeletronica.exception.FraudeNaVotacaoException;
import br.com.urnaeletronica.exception.JaVotouException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.repository.CandidatoRepository;
import br.com.urnaeletronica.repository.CargoRepository;
import br.com.urnaeletronica.repository.EleitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VotoServiceImpl implements VotoService {

    private final EleitorRepository eleitorRepository;
    private final CandidatoRepository  candidatoRepository;
    private final CargoRepository cargoRepository;

    @Override
    @Transactional
    public VotarResponse votar(VotarRequest request) {
        Eleitor eleitor = eleitorRepository.findByTituloEleitor(request.getTituloEleitor());

        validarEleitor(eleitor);
        validarVotacao(request);

        for (VotoRequest voto : request.getVotosEleitor()) {
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

    private void validarEleitor(Eleitor eleitor) throws JaVotouException, RecursoNaoEncontradoException {
        if(eleitor == null){
            throw new RecursoNaoEncontradoException("Eleitor não encontrado.");
        }
        if (eleitor.isJaVotou()){
            throw new JaVotouException();
        }
    }
    private void validarVotosDuplicados(VotarRequest request, Map<Long, Set<Long>> candidatosVotadosPorCargo) throws FraudeNaVotacaoException{
        for(VotoRequest votoRequest : request.getVotosEleitor()){
            // Votos em branco e nulo não possuem candidato, não precisam de validação de duplicidade
            if (votoRequest.getTipoVoto() == TipoVoto.BRANCO || votoRequest.getTipoVoto() == TipoVoto.NULO) {
                continue;
            }

            Long cargoId = votoRequest.getCargoID();
            Long candidatoNumero = votoRequest.getNumeroCandidato();

            candidatosVotadosPorCargo.putIfAbsent(cargoId, new  HashSet<>());

            Set<Long> candidatos = candidatosVotadosPorCargo.get(cargoId);

            if (!candidatos.add(candidatoNumero)) {
                throw new FraudeNaVotacaoException(
                        "Voto duplicado detectado: O candidato ao cargo " + cargoId+ " de número " + candidatoNumero + " recebeu mais de um voto para o mesmo cargo."
                );
            }
        }
    }
    private void validarLimiteCargos(List<Cargo> cargos,  Map<Long, Set<Long>> candidatosVotadosPorCargo) throws FraudeNaVotacaoException {
        for(Cargo cargo : cargos) {
            int votoNoCargo = candidatosVotadosPorCargo.get(cargo.getId()).size();
            if (votoNoCargo > cargo.getLimiteVotos()){
                throw new FraudeNaVotacaoException("O eleitor deseja votar no cargo além " +
                        "do limite de votos para o cargo");
            }
        }
    }
    private void validarCandidatosExistentes(VotarRequest request) throws FraudeNaVotacaoException {
        for (VotoRequest votoRequest : request.getVotosEleitor()) {
            if (votoRequest.getTipoVoto() != TipoVoto.NORMAL) {
                continue;
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

    private void validarVotacao(VotarRequest request) throws FraudeNaVotacaoException{
        validarCandidatosExistentes(request);

        // Guarda todos os votos do eleitor num map cujas chaves sao cagoID e numeroCandidato
        Map<Long, Set<Long>> candidatosVotadosPorCargo = new HashMap<>();

        validarVotosDuplicados(request, candidatosVotadosPorCargo);

        List<Cargo> cargos = cargoRepository.findAllById(candidatosVotadosPorCargo.keySet());

        validarLimiteCargos(cargos, candidatosVotadosPorCargo);
    }
}
