package br.com.urnaeletronica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cidadao")
public class Cidadao {
    @Id
    @Column(name = "cpf", length = 11)
    private String cpf;

    @Column(name = "nome", length = 60, nullable = false)
    private String nome;
}
