package br.com.urnaeletronica.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Requisição para apuração de resultados e determinação de vencedores.
 * A senha do TSE é obrigatória (US14 - Validar senha).
 */
@Getter
@Setter
public class ResultadoRequest {

    @NotBlank(message = "A senha é obrigatória")
    private String senha;
}
