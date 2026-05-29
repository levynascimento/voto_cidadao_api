package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.IdentificarEleitorResponse;
import br.com.urnaeletronica.entity.Eleitor;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.repository.EleitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EleitorServiceImpl implements EleitorService {

    private final EleitorRepository eleitorRepository;

    @Override
    @Transactional(readOnly = true)
    public IdentificarEleitorResponse identificarEleitor(String tituloEleitor) {
        Eleitor eleitor = eleitorRepository.findByTituloEleitor(tituloEleitor);
        if (eleitor == null) {
            throw new RecursoNaoEncontradoException("Eleitor nao encontrado");
        }

        return IdentificarEleitorResponse.builder()
                .id(eleitor.getId())
                .tituloEleitor(eleitor.getTituloEleitor())
                .nome(eleitor.getNome())
                .cpf(eleitor.getCpf())
                .jaVotou(eleitor.isJaVotou())
                .build();
    }
}
