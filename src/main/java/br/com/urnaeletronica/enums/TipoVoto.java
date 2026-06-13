package br.com.urnaeletronica.enums;

/**
 * Representa o tipo de voto registrado pelo eleitor em um determinado cargo.
 *
 * <ul>
 *     <li>{@link #NORMAL}: voto destinado a um candidato (exige número do candidato).</li>
 *     <li>{@link #BRANCO}: voto em branco (US17).</li>
 *     <li>{@link #NULO}: voto nulo (US18).</li>
 * </ul>
 */
public enum TipoVoto {
    NORMAL,
    BRANCO,
    NULO
}
