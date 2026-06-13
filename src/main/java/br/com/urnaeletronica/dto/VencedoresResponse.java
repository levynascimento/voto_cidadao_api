package br.com.urnaeletronica.dto;

import br.com.urnaeletronica.enums.UrnaEstado;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resposta com os vencedores de cada cargo (US13 - Determinar vencedores).
 */
@Builder
public record VencedoresResponse(
        UrnaEstado estadoUrna,
        LocalDateTime geradoEm,
        List<VencedorCargoResponse> vencedoresPorCargo
) {
}
