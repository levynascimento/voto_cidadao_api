package br.com.urnaeletronica.controller;

import br.com.urnaeletronica.dto.VotarRequest;
import br.com.urnaeletronica.dto.VotarResponse;
import br.com.urnaeletronica.service.VotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/voto")
@RequiredArgsConstructor
public class VotoController {

    private final VotoService votoService;

    @PostMapping("/votar")
    public ResponseEntity<VotarResponse> votar(
            @Valid @RequestBody VotarRequest request) {

        VotarResponse response = votoService.votar(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
