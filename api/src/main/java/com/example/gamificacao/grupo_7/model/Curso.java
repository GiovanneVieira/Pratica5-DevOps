package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Curso {

    private UUID id;

    private String name;

    private CursoStatus status;

    private Double notaFinal;

    public void conclui() {
        if (this.notaFinal > 7) {
            this.setStatus(CursoStatus.CONCLUIDO);
            return;
        }
        this.setStatus(CursoStatus.REPROVADO);
    }


}
