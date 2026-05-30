package br.com.urnaeletronica.service;

import br.com.urnaeletronica.dto.IdentificarEleitorResponse;

public interface EleitorService {

    IdentificarEleitorResponse identificarEleitor(String tituloEleitor);
}
