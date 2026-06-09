package br.com.urnaeletronica.dto;

import br.com.urnaeletronica.enums.TipoVoto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VotoRequest {
    @NotNull(message = "O ID do cargo é obrigatório.")
    private Long cargoID;

    private Long numeroCandidato;

    @NotNull(message = "O tipo de voto é obrigatório.")
    private TipoVoto tipoVoto = TipoVoto.NORMAL;
}
