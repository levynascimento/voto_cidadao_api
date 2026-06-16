package br.com.urnaeletronica.security;

import br.com.urnaeletronica.exception.SenhaInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários do {@link SenhaTseValidator} (US14 - Validar senha).
 */
@DisplayName("SenhaTseValidator - validação da senha do TSE")
class SenhaTseValidatorTest {

    private static final String SENHA_OFICIAL = "TSE2024";

    private SenhaTseValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SenhaTseValidator(SENHA_OFICIAL);
    }

    @Test
    @DisplayName("Não deve lançar exceção quando a senha for válida")
    void deveAceitarSenhaValida() {
        assertThatCode(() -> validator.validar(SENHA_OFICIAL)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha for diferente da oficial")
    void deveRejeitarSenhaDiferente() {
        assertThatThrownBy(() -> validator.validar("ERRADA"))
                .isInstanceOf(SenhaInvalidaException.class)
                .hasMessageContaining("Senha do TSE inválida");
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha for nula")
    void deveRejeitarSenhaNula() {
        assertThatThrownBy(() -> validator.validar(null))
                .isInstanceOf(SenhaInvalidaException.class)
                .hasMessageContaining("obrigatória");
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha estiver em branco")
    void deveRejeitarSenhaEmBranco() {
        assertThatThrownBy(() -> validator.validar("   "))
                .isInstanceOf(SenhaInvalidaException.class)
                .hasMessageContaining("obrigatória");
    }
}
