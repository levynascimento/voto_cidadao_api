package br.com.urnaeletronica.repository;

import br.com.urnaeletronica.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
    List<Cargo> getAllById(Long id);

    @Modifying
    @Query("UPDATE Cargo c SET c.votosBrancos = c.votosBrancos + 1 WHERE c.id = :cargoId")
    void incrementarVotosBrancos(@Param("cargoId") Long cargoId);

    @Modifying
    @Query("UPDATE Cargo c SET c.votosNulos = c.votosNulos + 1 WHERE c.id = :cargoId")
    void incrementarVotosNulos(@Param("cargoId") Long cargoId);
}
