package br.com.urnaeletronica.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentificarEleitorRequest {

    @NotBlank(message = "O título de eleitor é obrigatório")
    private String tituloEleitor;
}
