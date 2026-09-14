package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Curso {

    @NotNull(message = "O id do curso não pode ser nulo")
    private UUID id;

    @NotBlank(message = "O nome do curso não pode ser nulo ou vazio")
    private String name;

    @NotNull(message = "O status do curso não pode ser nulo")
    private CursoStatus status;

    @NotNull(message = "A nota final do curso não pode ser nula")
    @PositiveOrZero(message = "A nota final do curso deve ser positiva ou zero")
    private Double notaFinal;

    public void conclui() {
        if (this.notaFinal > 7) {
            this.setStatus(CursoStatus.CONCLUIDO);
            return;
        }
        this.setStatus(CursoStatus.REPROVADO);
    }


}
