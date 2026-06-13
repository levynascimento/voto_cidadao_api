package br.com.urnaeletronica.dto;

import lombok.Builder;

import java.util.List;

/**
 * Resultado consolidado de um cargo (US12 - Exibir resultados).
 * Mostra votos por candidato, percentuais (US11) e votos em branco/nulos.
 */
@Builder
public record ResultadoCargoResponse(
        Long cargoId,
        String cargoNome,
        int totalVotosValidos,
        int votosBrancos,
        int votosNulos,
        int totalGeral,
        double percentualBrancos,
        double percentualNulos,
        List<CandidatoResultadoResponse> candidatos,
        List<CandidatoResultadoResponse> vencedores
) {
}
