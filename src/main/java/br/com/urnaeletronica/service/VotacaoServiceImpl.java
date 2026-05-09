package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.FinalizarVotacaoResponse;
import br.com.urnaeletronica.dto.InicializarVotacaoResponse;
import br.com.urnaeletronica.entity.Urna;
import br.com.urnaeletronica.enums.UrnaEstado;
import br.com.urnaeletronica.exception.EstadoUrnaInvalidoException;
import br.com.urnaeletronica.exception.RecursoNaoEncontradoException;
import br.com.urnaeletronica.exception.SenhaInvalidaException;
import br.com.urnaeletronica.repository.UrnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VotacaoServiceImpl implements VotacaoService {

    private static final long ID_UNICO_URNA = 1L;

    private final UrnaRepository urnaRepository;

    @Value("${urna.tse.senha}")
    private String senhaOficialTse;

    @Override
    @Transactional
    public InicializarVotacaoResponse inicializarVotacao(String senhaInformada) {
        Urna urna = urnaRepository.findById(ID_UNICO_URNA)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Urna não encontrada para inicialização"));

        validarSenha(senhaInformada);
        validarEstadoAtualParaInicio(urna.getEstado());

        urna.setEstado(UrnaEstado.EM_VOTACAO);
        urna.setInicioVotacao(LocalDateTime.now());
        urnaRepository.save(urna);

        return InicializarVotacaoResponse.builder()
                .mensagem("Votação iniciada com sucesso")
                .estado(urna.getEstado())
                .inicioVotacao(urna.getInicioVotacao())
                .build();
    }

    @Override
    @Transactional
    public FinalizarVotacaoResponse finalizarVotacao(String senhaInformada) {
        Urna urna = urnaRepository.findById(ID_UNICO_URNA)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Urna não encontrada para inicialização"));

        validarSenha(senhaInformada);
        validarEstadoAtualParaFinalizar(urna.getEstado());

        urna.setEstado(UrnaEstado.FINALIZADA);
        urna.setInicioVotacao(LocalDateTime.now());
        urnaRepository.save(urna);

        return FinalizarVotacaoResponse.builder()
                .mensagem("Votação finalizada com sucesso.")
                .estado(urna.getEstado())
                .fimVotacao(urna.getInicioVotacao())
                .build();
    }

    @Override
    @Transactional
    public void garantirUrnaInicializada() {
        if (urnaRepository.existsById(ID_UNICO_URNA)) {
            return;
        }

        Urna urna = Urna.builder()
                .id(ID_UNICO_URNA)
                .estado(UrnaEstado.AGUARDANDO_INICIO)
                .build();

        urnaRepository.save(urna);
    }

    private void validarSenha(String senhaInformada) {
        if (!senhaOficialTse.equals(senhaInformada)) {
            throw new SenhaInvalidaException("Senha do TSE inválida");
        }
    }

    private void validarEstadoAtualParaInicio(UrnaEstado estadoAtual) {
        if (estadoAtual == UrnaEstado.EM_VOTACAO) {
            throw new EstadoUrnaInvalidoException("A votação já está em andamento");
        }

        if (estadoAtual == UrnaEstado.FINALIZADA) {
            throw new EstadoUrnaInvalidoException("A votação já foi finalizada e não pode ser reiniciada");
        }
    }

    private void validarEstadoAtualParaFinalizar(UrnaEstado estadoAtual) {
        if (estadoAtual == UrnaEstado.AGUARDANDO_INICIO) {
            throw new EstadoUrnaInvalidoException("A votação não foi iniciada.");
        }

        if (estadoAtual == UrnaEstado.FINALIZADA) {
            throw new EstadoUrnaInvalidoException("A votação já foi finalizada.");
        }
    }
}
