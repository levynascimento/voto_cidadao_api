package br.com.urnaeletronica.entity;

import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cargo")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "limite_votos_sessao", nullable = false)
    private Integer limiteVotos = 1;

    @Column(name = "votos_brancos")
    @Builder.Default
    private Integer votosBrancos = 0;

    @Column(name = "votos_nulos")
    @Builder.Default
    private Integer votosNulos = 0;
}
