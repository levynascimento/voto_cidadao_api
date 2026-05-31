package br.com.urnaeletronica.dto;

import br.com.urnaeletronica.entity.Voto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VotarRequest {

    @NotBlank(message = "O título de eleitor é obrigatório")
    private String tituloEleitor;

    @Size(min = 0, max = 3)
    private List<VotoRequest> votosEleitor;
}
