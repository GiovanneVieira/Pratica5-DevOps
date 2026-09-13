package com.example.gamificacao.grupo_7.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "table_aluno")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "aluno_id", updatable = false, nullable = false)
    private UUID id;
    private int cursosAdicionaisLiberados;
    private int progressoPremium;

    @Column(nullable = false)
    private String name;
    public int getCursosAdicionaisLiberados() {
        return cursosAdicionaisLiberados;
    }

    public int getProgressoPremium() {
        return progressoPremium;
    }
    public void concluirCurso(Curso curso, double notaFinal) {
        // funcionalidade ainda não implementada
    }

}
