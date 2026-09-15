package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "aluno")
@Table(name = "alunos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "ra", nullable = false)
    @Embedded
    private RA ra;

    @Enumerated(EnumType.STRING)
    @Column(name = "plano", nullable = false)
    private Plano plano;

    @Column(name = "moedas")
    private Integer moedas;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
