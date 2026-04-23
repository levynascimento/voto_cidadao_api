package br.com.urnaeletronica.repository;

import br.com.urnaeletronica.entity.Urna;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrnaRepository extends JpaRepository<Urna, Long> {
}
