package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.dto.ResultadoRequest;
import br.com.urnaeletronica.dto.ResultadoResponse;
import br.com.urnaeletronica.dto.VencedoresResponse;
import br.com.urnaeletronica.service.ResultadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de apuração de resultados.
 *
 * <ul>
 *     <li>US11 - Calcular percentuais (incluso na apuração)</li>
 *     <li>US12 - Exibir resultados</li>
 *     <li>US13 - Determinar vencedores</li>
 *     <li>US14 - Validar senha (a senha do TSE é exigida em ambos os endpoints)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/resultados")
@RequiredArgsConstructor
public class ResultadoController {

    private final ResultadoService resultadoService;

    /**
     * US11 + US12 - Apura e exibe os resultados (votos, percentuais, brancos e nulos).
     */
    @PostMapping
    public ResponseEntity<ResultadoResponse> apurarResultados(
            @Valid @RequestBody ResultadoRequest request) {

        ResultadoResponse response = resultadoService.apurarResultados(request.getSenha());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * US13 - Determina os vencedores de cada cargo.
     */
    @PostMapping("/vencedores")
    public ResponseEntity<VencedoresResponse> determinarVencedores(
            @Valid @RequestBody ResultadoRequest request) {

        VencedoresResponse response = resultadoService.determinarVencedores(request.getSenha());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
