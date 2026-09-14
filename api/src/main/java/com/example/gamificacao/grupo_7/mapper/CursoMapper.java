package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.model.Curso;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CursoMapper {

    public Curso buildEntity(String name, Double notaFinal){
        return Curso.builder()
                .id(UUID.randomUUID())
                .name(name)
                .status(CursoStatus.INICIADO)
                .notaFinal(notaFinal)
                .build();
    }

}
