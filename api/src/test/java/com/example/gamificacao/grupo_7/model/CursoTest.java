package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CursoTest {

    private Curso buildCursoComNota(Double notaFinal){
        return Curso.builder()
                .name("Curso Teste")
                .status(CursoStatus.INICIADO)
                .notaFinal(notaFinal)
                .build();
    }

    @Test
    void cursoComNotaSuperiorASeteDeveFicarConcluido(){
        var curso = this.buildCursoComNota(7.1);

        curso.conclui();

        assertEquals(CursoStatus.CONCLUIDO, curso.getStatus());
    }

    @Test
    void cursoComNotaIgualASeteDeveFicarReprovado(){
        var curso = this.buildCursoComNota(7.0);

        curso.conclui();

        assertEquals(CursoStatus.REPROVADO, curso.getStatus());
    }

    @Test
    void cursoComNotaInferiorASeteDeveFicarReprovado(){
        var curso = this.buildCursoComNota(6.5);

        curso.conclui();

        assertEquals(CursoStatus.REPROVADO, curso.getStatus());
    }

}
