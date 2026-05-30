package br.com.urnaeletronica.dto;

import lombok.Builder;

@Builder
public record IdentificarEleitorResponse(
        Long id,
        String tituloEleitor,
        String nome,
        String cpf,
        boolean jaVotou
) {
}
