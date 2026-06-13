package br.com.urnaeletronica.dto;

import br.com.urnaeletronica.enums.UrnaEstado;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resposta completa da apuração (US11 + US12).
 * O campo {@code parcial} indica se a votação ainda está em andamento.
 */
@Builder
public record ResultadoResponse(
        UrnaEstado estadoUrna,
        boolean parcial,
        LocalDateTime geradoEm,
        List<ResultadoCargoResponse> resultados
) {
}
