package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.VotarRequest;
import br.com.urnaeletronica.dto.VotarResponse;

public interface VotoService {

    VotarResponse votar(VotarRequest request);
}
