package br.com.urnaeletronica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VotoRequest {
    @NotNull(message = "O ID do cargo é obrigatório.")
    private Long cargoID;
    @NotNull(message = "O número do candidato é obrigatório.")
    private Long numeroCandidato;
}
