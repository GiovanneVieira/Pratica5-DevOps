package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "curso")
@Table(name = "cursos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private CursoStatus status;

    private Double notaFinal;

    /**
     * TDD3 - GREEN (transicao de estado compartilhada com TDD1/TDD2):
     * Regra de nota final derivada do cenario BDD da planilha:
     *
     *   Dado um aluno matriculado em um curso da grade
     *   Quando o aluno conclui o curso e obtem nota final igual ou inferior a 7,0
     *   Entao nenhum curso adicional deve ser liberado
     *   E o progresso para o plano Premium nao deve ser incrementado
     *
     * A nota exatamente igual a 7,0 reprova (os cenarios TDD1/TDD2 exigem nota
     * "superior a 7,0" para liberar recompensas). O status resultante alimenta a
     * PoliticaProgressao, que decide as recompensas de cada cenario.
     */
    public void conclui() {
        if (this.notaFinal > 7) {
            this.setStatus(CursoStatus.CONCLUIDO);
            return;
        }
        this.setStatus(CursoStatus.REPROVADO);
    }
}
