package br.com.urnaeletronica.dto;

import lombok.Builder;

/**
 * Resultado individual de um candidato dentro de um cargo.
 * Inclui a quantidade de votos e o percentual sobre os votos válidos (US11).
 */
@Builder
public record CandidatoResultadoResponse(
        Integer numeroCandidato,
        String nome,
        String fotoUrl,
        int votos,
        double percentualVotosValidos,
        boolean eleito
) {
}
