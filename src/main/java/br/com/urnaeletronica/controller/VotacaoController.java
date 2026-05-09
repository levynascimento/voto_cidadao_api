package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.dto.FinalizarVotacaoRequest;
import br.com.urnaeletronica.dto.FinalizarVotacaoResponse;
import br.com.urnaeletronica.dto.InicializarVotacaoRequest;
import br.com.urnaeletronica.dto.InicializarVotacaoResponse;
import br.com.urnaeletronica.service.VotacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/votacao")
@RequiredArgsConstructor
public class VotacaoController {

    private final VotacaoService votacaoService;

    @PostMapping("/inicializar")
    public ResponseEntity<InicializarVotacaoResponse> inicializarVotacao(
            @Valid @RequestBody InicializarVotacaoRequest request) {

        InicializarVotacaoResponse response = votacaoService.inicializarVotacao(request.getSenha());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/encerrar")
    public ResponseEntity<FinalizarVotacaoResponse> finalizarVotacao(
            @Valid
            @RequestBody FinalizarVotacaoRequest request
            )
    {
        FinalizarVotacaoResponse response = votacaoService.finalizarVotacao(request.getSenha());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
