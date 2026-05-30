package br.com.urnaeletronica.repository;

import br.com.urnaeletronica.entity.Cidadao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CidadaoRepository extends JpaRepository<Cidadao, Long> {
}
