package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.ResultadoResponse;
import br.com.urnaeletronica.dto.VencedoresResponse;

/**
 * Contrato para apuração de resultados da eleição.
 *
 * <ul>
 *     <li>US11 - Calcular percentuais</li>
 *     <li>US12 - Exibir resultados</li>
 *     <li>US13 - Determinar vencedores</li>
 *     <li>US14 - Validar senha (aplicada antes de cada operação)</li>
 * </ul>
 */
public interface ResultadoService {

    /**
     * Apura e exibe os resultados completos da eleição (US11 + US12).
     *
     * @param senhaInformada senha do TSE (US14)
     * @return resultados consolidados por cargo
     */
    ResultadoResponse apurarResultados(String senhaInformada);

    /**
     * Determina os vencedores de cada cargo (US13).
     *
     * @param senhaInformada senha do TSE (US14)
     * @return vencedores por cargo
     */
    VencedoresResponse determinarVencedores(String senhaInformada);
}
