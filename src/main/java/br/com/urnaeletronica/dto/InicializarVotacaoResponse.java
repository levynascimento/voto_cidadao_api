package br.com.urnaeletronica.dto;

import br.com.urnaeletronica.enums.UrnaEstado;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InicializarVotacaoResponse(
        String mensagem,
        UrnaEstado estado,
        LocalDateTime inicioVotacao
) {
}
