package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TDD3 - teste de unidade da transicao de estado que sustenta o cenario:
 *
 *   Quando o aluno conclui o curso
 *   E obtem nota final igual ou inferior a 7,0
 *   Entao nenhum curso adicional deve ser liberado
 *   E o progresso para o plano Premium nao deve ser incrementado
 *
 * A reprovação (status REPROVADO) e o que impede a liberacao de bonus na
 * PoliticaProgressao. A nota exatamente igual a 7,0 reprova porque os cenarios
 * TDD1/TDD2 exigem nota "superior a 7,0".
 */
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
