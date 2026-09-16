package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Matricula;
import org.springframework.stereotype.Component;

@Component
public class MatriculaMapper {

    public Matricula toEntity(Aluno aluno, Curso curso){
        return Matricula.builder()
                .aluno(aluno)
                .curso(curso)
                .build();
    }

    public MatriculaResponseDTO toResponseDTO(Matricula matricula){
        var curso = matricula.getCurso();
        return new MatriculaResponseDTO(
                matricula.getId(),
                curso.getName(),
                curso.getStatus(),
                curso.getNotaFinal()
        );
    }

}
