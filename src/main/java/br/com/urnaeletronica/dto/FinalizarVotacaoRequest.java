package br.com.urnaeletronica.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class FinalizarVotacaoRequest {

    @NotBlank(message = "A senha é obrigatória")
    private String senha;
}

