package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.VotarRequest;
import br.com.urnaeletronica.dto.VotarResponse;
import br.com.urnaeletronica.dto.VotoRequest;
import br.com.urnaeletronica.entity.Candidato;
import br.com.urnaeletronica.entity.Cargo;
import br.com.urnaeletronica.entity.Eleitor;
import br.com.urnaeletronica.entity.Voto;
import br.com.urnaeletronica.exception.FraudeNaVotacaoException;
import br.com.urnaeletronica.exception.JaVotouException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.repository.CandidatoRepository;
import br.com.urnaeletronica.repository.CargoRepository;
import br.com.urnaeletronica.repository.EleitorRepository;
import br.com.urnaeletronica.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            candidatoRepository.incrementarVoto(voto.getNumeroCandidato(), voto.getCargoID());
        }
        eleitor.setJaVotou(true);
        eleitorRepository.save(eleitor);

        return new VotarResponse("Votação computada com sucesso!");
    }

    private void validarEleitor(Eleitor eleitor) throws JaVotouException, RecursoNaoEncontradoException {
        if(eleitor == null){
            throw new RecursoNaoEncontradoException("Eleitor não encontrado.");
        }
        if (eleitor.isJaVotou()){
            throw new JaVotouException();
        }
    }

    private void validarVotacao(VotarRequest request) throws FraudeNaVotacaoException{
        // Guarda todos os votos do eleitor num map cujas chaves sao cagoID e numeroCandidato
        Map<Long, Long> votosEleitor = new HashMap<>();

        for(VotoRequest votoRequest : request.getVotosEleitor()){
            Long cargoId = votoRequest.getCargoID();
            votosEleitor.put(cargoId, votosEleitor.getOrDefault(cargoId, 0L) + 1L);
        }

        List<Cargo> cargos = cargoRepository.findAllById(votosEleitor.keySet());

        for(Cargo cargo : cargos) {
            Long votoNoCargo = votosEleitor.get(cargo.getId());
            if (votoNoCargo > cargo.getLimiteVotos()){
                throw new FraudeNaVotacaoException("O eleitor deseja votar no cargo além " +
                        "do limite de votos para o cargo");
            }
        }
    }
}
