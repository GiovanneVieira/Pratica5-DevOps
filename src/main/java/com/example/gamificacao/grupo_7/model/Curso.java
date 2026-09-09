package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import jakarta.persistence.*;
import lombok.*;

import javax.annotation.processing.Generated;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "curso_table")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(value = EnumType.STRING)
    private CursoStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

}
