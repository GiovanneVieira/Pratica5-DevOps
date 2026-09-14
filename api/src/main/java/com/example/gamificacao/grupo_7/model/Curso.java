package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import jakarta.persistence.*;
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

    @Enumerated(value = EnumType.STRING)
    private CursoStatus status;

    public Curso(String name, Aluno aluno){
        this.id = UUID.randomUUID();
        this.name = name;
        this.status = CursoStatus.INICIADO;
    }


}
