package br.com.urnaeletronica.dto;

import lombok.Builder;

@Builder
public record VotarResponse(
        String mensagem
) {
}
