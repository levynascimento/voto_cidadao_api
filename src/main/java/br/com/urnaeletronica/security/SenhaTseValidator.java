package br.com.urnaeletronica.security;

import br.com.urnaeletronica.exception.SenhaInvalidaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * US14 - Validar senha.
 *
 * <p>Componente reutilizável responsável por validar a senha oficial do TSE antes
 * de permitir a execução de ações sensíveis (inicializar/finalizar votação e
 * apuração de resultados). Centraliza a regra de "senha obrigatória" e
 * "validar antes de ações", garantindo segurança de forma consistente.</p>
 */
@Component
public class SenhaTseValidator {

    private final String senhaOficialTse;

    public SenhaTseValidator(@Value("${urna.tse.senha}") String senhaOficialTse) {
        this.senhaOficialTse = senhaOficialTse;
    }

    /**
     * Valida a senha informada contra a senha oficial do TSE.
     *
     * @param senhaInformada senha enviada na requisição
     * @throws SenhaInvalidaException se a senha estiver em branco ou for diferente da oficial
     */
    public void validar(String senhaInformada) {
        if (senhaInformada == null || senhaInformada.isBlank()) {
            throw new SenhaInvalidaException("A senha é obrigatória");
        }
        if (!Objects.equals(senhaOficialTse, senhaInformada)) {
            throw new SenhaInvalidaException("Senha do TSE inválida");
        }
    }
}
