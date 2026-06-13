package br.com.urnaeletronica.repository;

import br.com.urnaeletronica.entity.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CandidatoRepository extends JpaRepository<Candidato, Long> {

    boolean existsByNumeroCandidatoAndCargoId(Long numeroCandidato, Long cargoId);

    /**
     * Retorna todos os candidatos de um cargo ordenados do mais votado para o menos votado.
     * Utilizado na apuração (US11/US12) e na determinação de vencedores (US13).
     */
    List<Candidato> findByCargoIdOrderByNumeroVotosDesc(Long cargoId);

    @Modifying
    @Query("UPDATE Candidato c SET c.numeroVotos = c.numeroVotos + 1 WHERE c.numeroCandidato = :numero AND c.cargo.id = :cargoId")
    void incrementarVoto(@Param("numero") Long numero, @Param("cargoId") Long cargoId);
}
