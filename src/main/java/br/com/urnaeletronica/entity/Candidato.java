package br.com.urnaeletronica.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "candidato")
public class Candidato  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_candidato")
    private Integer numeroCandidato;

    @OneToOne
    @JoinColumn(name = "cpf", referencedColumnName = "cpf")
    private Cidadao cidadao;

    @ManyToOne
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @Column(name = "numero_votos")
    private Integer numeroVotos;

    @Column(name = "status_eleicao")
    private Integer statusEleicao;

    @Column(name = "foto_url")
    private String fotoUrl;
}
