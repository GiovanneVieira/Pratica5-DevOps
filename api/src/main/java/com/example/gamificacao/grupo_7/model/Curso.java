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


    // Atributos e metodo para testes anteriores realizados (legados por assim dizer)
    private CursoStatus status;

    private Double notaFinal;

    public void conclui() {
        if (this.notaFinal > 7) {
            this.setStatus(CursoStatus.CONCLUIDO);
            return;
        }
        this.setStatus(CursoStatus.REPROVADO);
    }
    //
}
