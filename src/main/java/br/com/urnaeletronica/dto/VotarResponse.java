package br.com.urnaeletronica.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VotarResponse(
        String mensagem,
        String nomeEleitor,
        LocalDateTime dataVotacao
) {
}
