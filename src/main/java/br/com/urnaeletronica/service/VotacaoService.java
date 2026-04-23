package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.InicializarVotacaoResponse;

public interface VotacaoService {

    InicializarVotacaoResponse inicializarVotacao(String senhaInformada);

    void garantirUrnaInicializada();
}
