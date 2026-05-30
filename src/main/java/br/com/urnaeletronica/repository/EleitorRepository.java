package br.com.urnaeletronica.repository;

import br.com.urnaeletronica.entity.Eleitor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EleitorRepository extends JpaRepository<Eleitor, Long> {
    Eleitor findByTituloEleitor(String tituloEleitor);
}
