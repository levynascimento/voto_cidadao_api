package br.com.urnaeletronica.repository;

import br.com.urnaeletronica.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
}
