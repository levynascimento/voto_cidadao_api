package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.dto.IdentificarEleitorRequest;
import br.com.urnaeletronica.dto.IdentificarEleitorResponse;
import br.com.urnaeletronica.service.EleitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eleitor")
@RequiredArgsConstructor
public class EleitorController {

    private final EleitorService eleitorService;

    @PostMapping("/identificar")
    public ResponseEntity<IdentificarEleitorResponse> identificarEleitor(
            @Valid @RequestBody IdentificarEleitorRequest request) {

        IdentificarEleitorResponse response = eleitorService.identificarEleitor(request.getTituloEleitor());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
