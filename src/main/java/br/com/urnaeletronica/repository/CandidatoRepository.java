package br.com.urnaeletronica.repository;

import br.com.urnaeletronica.entity.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatoRepository extends JpaRepository<Candidato, Long> {
}
