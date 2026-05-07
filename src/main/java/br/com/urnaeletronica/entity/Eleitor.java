package br.com.urnaeletronica.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eleitor")
public class Eleitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo_eleitor", unique = true)
    private String tituloEleitor;

    @Column(name = "ja_votou")
    private boolean jaVotou;

    @OneToOne
    @JoinColumn(name = "cpf", referencedColumnName = "cpf")
    private Cidadao cidadao;
}
