package br.com.urnaeletronica.dto;

import lombok.Builder;

import java.util.List;

/**
 * Vencedores de um cargo específico (US13 - Determinar vencedores).
 */
@Builder
public record VencedorCargoResponse(
        Long cargoId,
        String cargoNome,
        int vagas,
        List<CandidatoResultadoResponse> vencedores
) {
}
